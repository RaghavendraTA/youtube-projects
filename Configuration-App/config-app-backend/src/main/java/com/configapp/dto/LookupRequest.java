package com.configapp.dto;

import java.util.List;

public class LookupRequest {
    private List<String> settings;  // Setting GUIDs
    private List<String> keys;      // Setting keys

    public LookupRequest() {}

    public LookupRequest(List<String> settings, List<String> keys) {
        this.settings = settings;
        this.keys = keys;
    }

    public List<String> getSettings() {
        return settings;
    }

    public void setSettings(List<String> settings) {
        this.settings = settings;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> settings;
        private List<String> keys;

        public Builder settings(List<String> settings) {
            this.settings = settings;
            return this;
        }

        public Builder keys(List<String> keys) {
            this.keys = keys;
            return this;
        }

        public LookupRequest build() {
            return new LookupRequest(settings, keys);
        }
    }
}
