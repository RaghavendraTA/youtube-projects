package com.configapp.service;

import com.configapp.dto.CreateConfigurationRequest;
import com.configapp.dto.LookupRequest;
import com.configapp.model.Setting;
import com.configapp.repository.ConfigurationRepository;
import com.configapp.repository.ConfigurationStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LookupServiceTest {

    @Autowired
    private LookupService lookupService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ConfigurationStatsRepository statsRepository;

    private String testUserId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        configurationRepository.deleteAll();
        statsRepository.deleteAll();
    }

    @Test
    void testLookupBySettingGuid() throws ExecutionException, InterruptedException {
        // Create configuration with settings
        String settingId1 = UUID.randomUUID().toString();
        String settingId2 = UUID.randomUUID().toString();

        List<Setting> settings = List.of(
                Setting.builder()
                        .id(settingId1)
                        .key("app.name")
                        .value("MyApp")
                        .settingType("string")
                        .build(),
                Setting.builder()
                        .id(settingId2)
                        .key("app.version")
                        .value("1.0.0")
                        .settingType("string")
                        .build()
        );

        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(settings)
                .build();

        var config = configurationService.createConfiguration(testUserId, request).get();

        // Lookup by setting GUID
        LookupRequest lookupRequest = LookupRequest.builder()
                .settings(List.of(settingId1))
                .build();

        var result = lookupService.lookup(config.getConfigurationId(), lookupRequest).get();

        @SuppressWarnings("unchecked")
        List<Setting> resultSettings = (List<Setting>) result.get("settings");
        assertEquals(1, resultSettings.size());
        assertEquals(settingId1, resultSettings.get(0).getId());
        assertEquals("app.name", resultSettings.get(0).getKey());
    }

    @Test
    void testLookupByKey() throws ExecutionException, InterruptedException {
        // Create configuration with settings
        String settingId1 = UUID.randomUUID().toString();
        String settingId2 = UUID.randomUUID().toString();

        List<Setting> settings = List.of(
                Setting.builder()
                        .id(settingId1)
                        .key("app.name")
                        .value("MyApp")
                        .settingType("string")
                        .build(),
                Setting.builder()
                        .id(settingId2)
                        .key("app.version")
                        .value("1.0.0")
                        .settingType("string")
                        .build()
        );

        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(settings)
                .build();

        var config = configurationService.createConfiguration(testUserId, request).get();

        // Lookup by key
        LookupRequest lookupRequest = LookupRequest.builder()
                .keys(List.of("app.version"))
                .build();

        var result = lookupService.lookup(config.getConfigurationId(), lookupRequest).get();

        @SuppressWarnings("unchecked")
        List<Setting> resultSettings = (List<Setting>) result.get("settings");
        assertEquals(1, resultSettings.size());
        assertEquals("app.version", resultSettings.get(0).getKey());
        assertEquals("1.0.0", resultSettings.get(0).getValue());
    }

    @Test
    void testLookupNoResults() throws ExecutionException, InterruptedException {
        List<Setting> settings = List.of(
                Setting.builder()
                        .id(UUID.randomUUID().toString())
                        .key("app.name")
                        .value("MyApp")
                        .settingType("string")
                        .build()
        );

        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(settings)
                .build();

        var config = configurationService.createConfiguration(testUserId, request).get();

        // Lookup for non-existent setting
        LookupRequest lookupRequest = LookupRequest.builder()
                .keys(List.of("nonexistent.key"))
                .build();

        var result = lookupService.lookup(config.getConfigurationId(), lookupRequest).get();

        @SuppressWarnings("unchecked")
        List<Setting> resultSettings = (List<Setting>) result.get("settings");
        assertEquals(0, resultSettings.size());
    }

    @Test
    void testLookupConfigurationNotFound() {
        LookupRequest lookupRequest = LookupRequest.builder()
                .keys(List.of("app.name"))
                .build();

        assertThrows(Exception.class, () ->
            lookupService.lookup("nonexistent-config-id", lookupRequest).get()
        );
    }

    @Test
    void testLookupUpdatesStats() throws ExecutionException, InterruptedException {
        List<Setting> settings = List.of(
                Setting.builder()
                        .id(UUID.randomUUID().toString())
                        .key("app.name")
                        .value("MyApp")
                        .settingType("string")
                        .build()
        );

        CreateConfigurationRequest request = CreateConfigurationRequest.builder()
                .name("Test Config")
                .description("Test")
                .settings(settings)
                .build();

        var config = configurationService.createConfiguration(testUserId, request).get();

        // Initial lookup
        LookupRequest lookupRequest = LookupRequest.builder()
                .keys(List.of("app.name"))
                .build();

        lookupService.lookup(config.getConfigurationId(), lookupRequest).get();

        // Small delay to allow async stats update
        Thread.sleep(500);

        var stats = configurationService.getConfigurationStats(
                config.getConfigurationId(), testUserId).get();

        assertNotNull(stats);
        assertEquals(1L, stats.getTotalLookups());
        assertNotNull(stats.getLastLookupAt());
    }
}
