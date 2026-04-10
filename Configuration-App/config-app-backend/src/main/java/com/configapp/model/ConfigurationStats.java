package com.configapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "configuration_stats")
public class ConfigurationStats {
    @Id
    private String id;

    @Indexed(unique = true)
    private String configurationId;

    private Long totalLookups;
    private LocalDateTime lastLookupAt;

    public ConfigurationStats() {}

    public ConfigurationStats(String id, String configurationId, Long totalLookups, LocalDateTime lastLookupAt) {
        this.id = id;
        this.configurationId = configurationId;
        this.totalLookups = totalLookups;
        this.lastLookupAt = lastLookupAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public Long getTotalLookups() {
        return totalLookups;
    }

    public void setTotalLookups(Long totalLookups) {
        this.totalLookups = totalLookups;
    }

    public LocalDateTime getLastLookupAt() {
        return lastLookupAt;
    }

    public void setLastLookupAt(LocalDateTime lastLookupAt) {
        this.lastLookupAt = lastLookupAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String configurationId;
        private Long totalLookups;
        private LocalDateTime lastLookupAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder configurationId(String configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        public Builder totalLookups(Long totalLookups) {
            this.totalLookups = totalLookups;
            return this;
        }

        public Builder lastLookupAt(LocalDateTime lastLookupAt) {
            this.lastLookupAt = lastLookupAt;
            return this;
        }

        public ConfigurationStats build() {
            return new ConfigurationStats(id, configurationId, totalLookups, lastLookupAt);
        }
    }
}
