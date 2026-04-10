package com.configapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Setting {
    private String id;
    private String key;
    private Object value;
    
    @JsonProperty("type")
    private String settingType;

    public Setting() {}

    public Setting(String id, String key, Object value, String settingType) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.settingType = settingType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    public String getSettingType() { return settingType; }
    public void setSettingType(String settingType) { this.settingType = settingType; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String key;
        private Object value;
        private String settingType;

        public Builder id(String id) { this.id = id; return this; }
        public Builder key(String key) { this.key = key; return this; }
        public Builder value(Object value) { this.value = value; return this; }
        public Builder settingType(String settingType) { this.settingType = settingType; return this; }

        public Setting build() {
            return new Setting(id, key, value, settingType);
        }
    }
}
