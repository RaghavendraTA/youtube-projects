package com.configapp.dto;

import java.time.LocalDateTime;

public class ConfigurationStatsDto {
    private String configurationId;
    private Long totalLookups;
    private LocalDateTime lastLookupAt;

    public ConfigurationStatsDto() {}

    public ConfigurationStatsDto(String configurationId, Long totalLookups, LocalDateTime lastLookupAt) {
        this.configurationId = configurationId;
        this.totalLookups = totalLookups;
        this.lastLookupAt = lastLookupAt;
    }

    public String getConfigurationId() { return configurationId; }
    public void setConfigurationId(String configurationId) { this.configurationId = configurationId; }
    public Long getTotalLookups() { return totalLookups; }
    public void setTotalLookups(Long totalLookups) { this.totalLookups = totalLookups; }
    public LocalDateTime getLastLookupAt() { return lastLookupAt; }
    public void setLastLookupAt(LocalDateTime lastLookupAt) { this.lastLookupAt = lastLookupAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String configurationId;
        private Long totalLookups;
        private LocalDateTime lastLookupAt;

        public Builder configurationId(String configurationId) { this.configurationId = configurationId; return this; }
        public Builder totalLookups(Long totalLookups) { this.totalLookups = totalLookups; return this; }
        public Builder lastLookupAt(LocalDateTime lastLookupAt) { this.lastLookupAt = lastLookupAt; return this; }

        public ConfigurationStatsDto build() {
            return new ConfigurationStatsDto(configurationId, totalLookups, lastLookupAt);
        }
    }
}
