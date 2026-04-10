package com.configapp.service;

import com.configapp.dto.CreateConfigurationRequest;
import com.configapp.dto.UpdateConfigurationRequest;
import com.configapp.dto.TransferOwnershipRequest;
import com.configapp.model.Configuration;
import com.configapp.model.Setting;
import com.configapp.repository.AdminRepository;
import com.configapp.repository.ConfigurationRepository;
import com.configapp.repository.ConfigurationStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConfigurationServiceTest {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ConfigurationStatsRepository statsRepository;

    @Autowired
    private AdminRepository adminRepository;

    private String testUserId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        configurationRepository.deleteAll();
        statsRepository.deleteAll();
    }

    @Test
    void testCreateConfiguration() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("A test configuration")
                .settings(List.of(
                        Setting.builder()
                                .id(UUID.randomUUID().toString())
                                .key("app.name")
                                .value("MyApp")
                                .settingType("string")
                                .build()
                ))
                .build();

        var result = configurationService.createConfiguration(testUserId, request).get();

        assertNotNull(result);
        assertEquals("Test Config", result.getName());
        assertEquals("A test configuration", result.getDescription());
        assertEquals(1L, result.getVersion());
        assertTrue(result.getActive());
        assertEquals(testUserId, result.getOwner());
        assertEquals(1, result.getSettings().size());
    }

    @Test
    void testGetUserConfigurations() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(List.of())
                .build();

        configurationService.createConfiguration(testUserId, request).get();
        var configs = configurationService.getConfigurationsByUser(testUserId).get();

        assertEquals(1, configs.size());
        assertEquals("Test Config", configs.get(0).getName());
    }

    @Test
    void testUpdateConfigurationCreatesNewVersion() throws ExecutionException, InterruptedException {
        // Create initial configuration
        CreateConfigurationRequest createRequest = CreateConfigurationRequest.builder()
                .name("Original Name")
                .description("Original description")
                .settings(List.of())
                .build();

        var created = configurationService.createConfiguration(testUserId, createRequest).get();

        // Update configuration
        UpdateConfigurationRequest updateRequest = UpdateConfigurationRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .settings(List.of())
                .build();

        var updated = configurationService.updateConfiguration(
                created.getConfigurationId(), testUserId, updateRequest).get();

        assertEquals("Updated Name", updated.getName());
        assertEquals(2L, updated.getVersion());
        assertTrue(updated.getActive());

        // Verify old version is inactive
        var allVersions = configurationRepository.findByConfigurationId(created.getConfigurationId());
        assertEquals(2, allVersions.size());
        
        long activeCount = allVersions.stream().filter(Configuration::getActive).count();
        assertEquals(1, activeCount);
    }

    @Test
    void testDeleteConfigurationAsOwner() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(List.of())
                .build();

        var created = configurationService.createConfiguration(testUserId, request).get();

        // Delete should succeed
        assertDoesNotThrow(() -> 
            configurationService.deleteConfiguration(created.getConfigurationId(), testUserId).get()
        );

        // Verify deleted
        var configs = configurationService.getConfigurationsByUser(testUserId).get();
        assertEquals(0, configs.size());
    }

    @Test
    void testDeleteConfigurationAsNonOwner() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(List.of())
                .build();

        var created = configurationService.createConfiguration(testUserId, request).get();
        String otherUserId = UUID.randomUUID().toString();

        // Delete should fail for non-owner
        assertThrows(Exception.class, () ->
            configurationService.deleteConfiguration(created.getConfigurationId(), otherUserId).get()
        );
    }

    @Test
    void testSetConfigurationVersion() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(List.of())
                .build();

        var created = configurationService.createConfiguration(testUserId, request).get();

        // Create version 2
        UpdateConfigurationRequest updateRequest = UpdateConfigurationRequest.builder()
                .name("Version 2")
                .description("Test")
                .settings(List.of())
                .build();

        configurationService.updateConfiguration(created.getConfigurationId(), testUserId, updateRequest).get();

        // Set version 1 as active
        configurationService.setConfigurationVersion(
                created.getConfigurationId(), 1L, testUserId).get();

        // Verify version 1 is active
        var allVersions = configurationRepository.findByConfigurationId(created.getConfigurationId());
        var activeVersion = allVersions.stream()
                .filter(Configuration::getActive)
                .findFirst()
                .orElseThrow();

        assertEquals(1L, activeVersion.getVersion());
    }

    @Test
    void testGetConfigurationHistory() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(List.of())
                .build();

        var created = configurationService.createConfiguration(testUserId, request).get();

        // Create multiple versions
        for (int i = 0; i < 3; i++) {
            UpdateConfigurationRequest updateRequest = UpdateConfigurationRequest.builder()
                    .name("Version " + (i + 2))
                    .description("Test")
                    .settings(List.of())
                    .build();
            configurationService.updateConfiguration(
                    created.getConfigurationId(), testUserId, updateRequest).get();
        }

        var history = configurationService.getConfigurationHistory(
                created.getConfigurationId(), testUserId).get();

        assertEquals(4, history.size());
    }

    @Test
    void testTransferOwnershipAsOwner() throws ExecutionException, InterruptedException {
        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(List.of())
                .build();

        var created = configurationService.createConfiguration(testUserId, request).get();
        String newOwnerId = UUID.randomUUID().toString();

        TransferOwnershipRequest transferRequest = TransferOwnershipRequest.builder()
                .transferTo(newOwnerId)
                .build();

        // Should fail because new owner doesn't exist in admin table
        assertThrows(Exception.class, () ->
            configurationService.transferOwnership(
                    created.getConfigurationId(), testUserId, transferRequest).get()
        );
    }
}
