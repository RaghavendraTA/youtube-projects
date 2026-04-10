package com.configapp.service;

import com.configapp.dto.LookupRequest;
import com.configapp.model.Configuration;
import com.configapp.model.ConfigurationStats;
import com.configapp.model.Setting;
import com.configapp.repository.ConfigurationRepository;
import com.configapp.repository.ConfigurationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class LookupService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ConfigurationStatsRepository statsRepository;

    @Async
    public CompletableFuture<Map<String, Object>> lookup(String configurationId, LookupRequest request) {
        Configuration config = configurationRepository
                .findByConfigurationIdAndActive(configurationId, true)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found or not active"));

        List<Setting> matchedSettings = List.of();

        if (request.getSettings() != null && !request.getSettings().isEmpty()) {
            // Look up by setting GUID
            matchedSettings = config.getSettings().stream()
                    .filter(s -> request.getSettings().contains(s.getId()))
                    .collect(Collectors.toList());
        } else if (request.getKeys() != null && !request.getKeys().isEmpty()) {
            // Look up by key
            matchedSettings = config.getSettings().stream()
                    .filter(s -> request.getKeys().contains(s.getKey()))
                    .collect(Collectors.toList());
        }

        // Update stats asynchronously
        updateStatsAsync(configurationId);

        // Return matched settings
        return CompletableFuture.completedFuture(
                Map.of("settings", matchedSettings)
        );
    }

    @Async
    private void updateStatsAsync(String configurationId) {
        try {
            ConfigurationStats stats = statsRepository.findByConfigurationId(configurationId)
                    .orElseGet(() -> ConfigurationStats.builder()
                            .id(UUID.randomUUID().toString())
                            .configurationId(configurationId)
                            .totalLookups(0L)
                            .build());

            stats.setTotalLookups(stats.getTotalLookups() + 1);
            stats.setLastLookupAt(LocalDateTime.now());
            statsRepository.save(stats);
        } catch (Exception e) {
            // Log but don't fail the lookup if stats update fails
            System.err.println("Failed to update stats: " + e.getMessage());
        }
    }
}
