package com.configapp.dto;

import com.configapp.model.Setting;
import java.util.List;
import java.util.Map;

public class UpdateConfigurationRequest {
    private String name;
    private String description;
    private List<Setting> settings;
    private Map<String, Object> additionalProperties;

    public UpdateConfigurationRequest() {}

    public UpdateConfigurationRequest(String name, String description, List<Setting> settings, Map<String, Object> additionalProperties) {
        this.name = name;
        this.description = description;
        this.settings = settings;
        this.additionalProperties = additionalProperties;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Setting> getSettings() { return settings; }
    public void setSettings(List<Setting> settings) { this.settings = settings; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String description;
        private List<Setting> settings;
        private Map<String, Object> additionalProperties;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder settings(List<Setting> settings) { this.settings = settings; return this; }
        public Builder additionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; return this; }

        public UpdateConfigurationRequest build() {
            return new UpdateConfigurationRequest(name, description, settings, additionalProperties);
        }
    }
}
