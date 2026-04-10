package com.configapp.service;

import com.configapp.dto.ConfigurationDto;
import com.configapp.dto.CreateConfigurationRequest;
import com.configapp.dto.HistoryEntryDto;
import com.configapp.dto.UpdateConfigurationRequest;
import com.configapp.dto.TransferOwnershipRequest;
import com.configapp.dto.ConfigurationStatsDto;
import com.configapp.model.Admin;
import com.configapp.model.Configuration;
import com.configapp.model.ConfigurationStats;
import com.configapp.repository.AdminRepository;
import com.configapp.repository.ConfigurationRepository;
import com.configapp.repository.ConfigurationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ConfigurationService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ConfigurationStatsRepository statsRepository;

    @Async
    public CompletableFuture<ConfigurationDto> createConfiguration(String userId, CreateConfigurationRequest request) {
        String configId = UUID.randomUUID().toString();

        Configuration config = Configuration.builder()
                .id(UUID.randomUUID().toString())
                .configurationId(configId)
                .name(request.getName())
                .description(request.getDescription())
                .version(1L)
                .active(true)
                .owner(userId)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .adminIds(List.of())
                .settings(request.getSettings() != null ? request.getSettings() : List.of())
                .additionalProperties(request.getAdditionalProperties())
                .build();

        Configuration savedConfig = configurationRepository.save(config);

        // Initialize stats
        ConfigurationStats stats = ConfigurationStats.builder()
                .id(UUID.randomUUID().toString())
                .configurationId(configId)
                .totalLookups(0L)
                .lastLookupAt(null)
                .build();
        statsRepository.save(stats);

        // Verify user exists
        Admin admin = adminRepository.findByUsername(userId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        admin.setNumberOfConfigurationsOwned(admin.getNumberOfConfigurationsOwned() + 1);
        adminRepository.save(admin);

        return CompletableFuture.completedFuture(toConfigurationDto(savedConfig));
    }

    @Async
    public CompletableFuture<List<ConfigurationDto>> getConfigurationsByUser(String userId) {
        List<Configuration> configs = configurationRepository.findActiveConfigurationsByUser(userId);
        return CompletableFuture.completedFuture(
                configs.stream().map(this::toConfigurationDto).collect(Collectors.toList())
        );
    }

    @Async
    public CompletableFuture<List<HistoryEntryDto>> getConfigurationHistory(String configurationId, String userId) {
        // Check if user has access to this configuration
        List<Configuration> allVersions = configurationRepository.findByConfigurationId(configurationId);
        if (allVersions.isEmpty()) {
            throw new IllegalArgumentException("Configuration not found");
        }

        // Validate user has access
        Configuration firstConfig = allVersions.get(0);
        if (!firstConfig.getOwner().equals(userId) && !firstConfig.getAdminIds().contains(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        List<HistoryEntryDto> history = allVersions.stream()
                .map(config -> HistoryEntryDto.builder()
                        .version(config.getVersion())
                        .lastModifiedDate(config.getLastModifiedDate())
                        .active(config.getActive())
                        .build())
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(history);
    }

    @Async
    public CompletableFuture<ConfigurationDto> updateConfiguration(String configurationId, String userId, UpdateConfigurationRequest request) {
        List<Configuration> allVersions = configurationRepository.findByConfigurationId(configurationId);
        
        Configuration active = allVersions.stream()
                .filter(Configuration::getActive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));

        // Verify user can edit
        if (!active.getOwner().equals(userId) && !active.getAdminIds().contains(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Deactivate current active version
        active.setActive(false);
        configurationRepository.save(active);

        // Create new version
        Long newVersion = active.getVersion() + 1;
        Configuration newConfig = Configuration.builder()
                .id(UUID.randomUUID().toString())
                .configurationId(configurationId)
                .name(request.getName() != null ? request.getName() : active.getName())
                .description(request.getDescription() != null ? request.getDescription() : active.getDescription())
                .version(newVersion)
                .active(true)
                .owner(active.getOwner())
                .createdDate(active.getCreatedDate())
                .lastModifiedDate(LocalDateTime.now())
                .adminIds(active.getAdminIds())
                .settings(request.getSettings() != null ? request.getSettings() : active.getSettings())
                .additionalProperties(request.getAdditionalProperties() != null ? request.getAdditionalProperties() : active.getAdditionalProperties())
                .build();

        Configuration savedConfig = configurationRepository.save(newConfig);
        return CompletableFuture.completedFuture(toConfigurationDto(savedConfig));
    }

    @Async
    public CompletableFuture<Void> deleteConfiguration(String configurationId, String userId) {
        List<Configuration> allVersions = configurationRepository.findByConfigurationId(configurationId);
        
        Configuration active = allVersions.stream()
                .filter(Configuration::getActive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));

        // Only OWNER can delete
        if (!active.getOwner().equals(userId)) {
            throw new IllegalArgumentException("Only owner can delete configuration");
        }

        // Delete all versions
        configurationRepository.deleteAll(allVersions);
        
        // Delete stats
        statsRepository.deleteById(allVersions.get(0).getId());

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> setConfigurationVersion(String configurationId, Long version, String userId) {
        List<Configuration> allVersions = configurationRepository.findByConfigurationId(configurationId);
        
        // Check access
        Configuration original = allVersions.stream()
                .filter(Configuration::getActive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));

        if (!original.getOwner().equals(userId) && !original.getAdminIds().contains(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Find the version to activate
        Configuration versionToActivate = allVersions.stream()
                .filter(c -> c.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        // Deactivate current
        allVersions.stream()
                .filter(Configuration::getActive)
                .forEach(c -> {
                    c.setActive(false);
                    configurationRepository.save(c);
                });

        // Activate new version
        versionToActivate.setActive(true);
        configurationRepository.save(versionToActivate);

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> transferOwnership(String configurationId, String userId, TransferOwnershipRequest request) {
        List<Configuration> allVersions = configurationRepository.findByConfigurationId(configurationId);
        
        Configuration config = allVersions.stream()
                .filter(Configuration::getActive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));

        // Only OWNER can transfer
        if (!config.getOwner().equals(userId)) {
            throw new IllegalArgumentException("Only owner can transfer ownership");
        }

        // Verify new owner exists
        Admin newOwner = adminRepository.findByUsername(request.getTransferTo())
                .orElseThrow(() -> new IllegalArgumentException("Target admin not found"));

        // Update all versions
        allVersions.forEach(c -> {
            c.setOwner(newOwner.getUsername());
            configurationRepository.save(c);
        });

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<ConfigurationStatsDto> getConfigurationStats(String configurationId, String userId) {
        List<Configuration> allVersions = configurationRepository.findByConfigurationId(configurationId);
        if (allVersions.isEmpty()) {
            throw new IllegalArgumentException("Configuration not found");
        }

        Configuration config = allVersions.get(0);
        if (!config.getOwner().equals(userId) && !config.getAdminIds().contains(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        ConfigurationStats stats = statsRepository.findByConfigurationId(configurationId)
                .orElseThrow(() -> new IllegalArgumentException("Stats not found"));

        return CompletableFuture.completedFuture(toConfigurationStatsDto(stats));
    }

    private ConfigurationDto toConfigurationDto(Configuration config) {
        return ConfigurationDto.builder()
                .id(config.getId())
                .configurationId(config.getConfigurationId())
                .name(config.getName())
                .description(config.getDescription())
                .version(config.getVersion())
                .active(config.getActive())
                .owner(config.getOwner())
                .createdDate(config.getCreatedDate())
                .lastModifiedDate(config.getLastModifiedDate())
                .adminIds(config.getAdminIds())
                .settings(config.getSettings())
                .additionalProperties(config.getAdditionalProperties())
                .build();
    }

    private ConfigurationStatsDto toConfigurationStatsDto(ConfigurationStats stats) {
        return ConfigurationStatsDto.builder()
                .configurationId(stats.getConfigurationId())
                .totalLookups(stats.getTotalLookups())
                .lastLookupAt(stats.getLastLookupAt())
                .build();
    }
}
