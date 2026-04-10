package com.configapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class AdminDto {
    private String id;
    private String username;
    private Long numberOfConfigurationsOwned;
    private LocalDateTime registeredOn;
    private Boolean active;

    public AdminDto() {}

    public AdminDto(String id, String username, Long numberOfConfigurationsOwned, LocalDateTime registeredOn, Boolean active) {
        this.id = id;
        this.username = username;
        this.numberOfConfigurationsOwned = numberOfConfigurationsOwned;
        this.registeredOn = registeredOn;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getNumberOfConfigurationsOwned() { return numberOfConfigurationsOwned; }
    public void setNumberOfConfigurationsOwned(Long numberOfConfigurationsOwned) { this.numberOfConfigurationsOwned = numberOfConfigurationsOwned; }
    public LocalDateTime getRegisteredOn() { return registeredOn; }
    public void setRegisteredOn(LocalDateTime registeredOn) { this.registeredOn = registeredOn; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String username;
        private Long numberOfConfigurationsOwned;
        private LocalDateTime registeredOn;
        private Boolean active;

        public Builder id(String id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder numberOfConfigurationsOwned(Long numberOfConfigurationsOwned) { this.numberOfConfigurationsOwned = numberOfConfigurationsOwned; return this; }
        public Builder registeredOn(LocalDateTime registeredOn) { this.registeredOn = registeredOn; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }

        public AdminDto build() {
            return new AdminDto(id, username, numberOfConfigurationsOwned, registeredOn, active);
        }
    }
}
