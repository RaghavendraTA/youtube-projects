package com.configapp.dto;

import com.configapp.model.Setting;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ConfigurationDto {
    private String id;
    private String configurationId;
    private String name;
    private String description;
    private Long version;
    private Boolean active;
    private String owner;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private List<String> adminIds;
    private List<Setting> settings;
    private Map<String, Object> additionalProperties;

    public ConfigurationDto() {}

    public ConfigurationDto(String id, String configurationId, String name, String description, Long version, Boolean active, String owner, LocalDateTime createdDate, LocalDateTime lastModifiedDate, List<String> adminIds, List<Setting> settings, Map<String, Object> additionalProperties) {
        this.id = id;
        this.configurationId = configurationId;
        this.name = name;
        this.description = description;
        this.version = version;
        this.active = active;
        this.owner = owner;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.adminIds = adminIds;
        this.settings = settings;
        this.additionalProperties = additionalProperties;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConfigurationId() { return configurationId; }
    public void setConfigurationId(String configurationId) { this.configurationId = configurationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
    public List<String> getAdminIds() { return adminIds; }
    public void setAdminIds(List<String> adminIds) { this.adminIds = adminIds; }
    public List<Setting> getSettings() { return settings; }
    public void setSettings(List<Setting> settings) { this.settings = settings; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String configurationId;
        private String name;
        private String description;
        private Long version;
        private Boolean active;
        private String owner;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private List<String> adminIds;
        private List<Setting> settings;
        private Map<String, Object> additionalProperties;

        public Builder id(String id) { this.id = id; return this; }
        public Builder configurationId(String configurationId) { this.configurationId = configurationId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder owner(String owner) { this.owner = owner; return this; }
        public Builder createdDate(LocalDateTime createdDate) { this.createdDate = createdDate; return this; }
        public Builder lastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; return this; }
        public Builder adminIds(List<String> adminIds) { this.adminIds = adminIds; return this; }
        public Builder settings(List<Setting> settings) { this.settings = settings; return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; return this; }

        public ConfigurationDto build() {
            return new ConfigurationDto(id, configurationId, name, description, version, active, owner, createdDate, lastModifiedDate, adminIds, settings, additionalProperties);
        }
    }
}
