package com.configapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "admins")
public class Admin {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;
    private Long numberOfConfigurationsOwned;
    private LocalDateTime registeredOn;
    private Boolean active;

    public Admin() {}

    public Admin(String id, String username, String password, Long numberOfConfigurationsOwned, LocalDateTime registeredOn, Boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.numberOfConfigurationsOwned = numberOfConfigurationsOwned;
        this.registeredOn = registeredOn;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
        private String password;
        private Long numberOfConfigurationsOwned;
        private LocalDateTime registeredOn;
        private Boolean active;

        public Builder id(String id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder numberOfConfigurationsOwned(Long numberOfConfigurationsOwned) { this.numberOfConfigurationsOwned = numberOfConfigurationsOwned; return this; }
        public Builder registeredOn(LocalDateTime registeredOn) { this.registeredOn = registeredOn; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }

        public Admin build() {
            return new Admin(id, username, password, numberOfConfigurationsOwned, registeredOn, active);
        }
    }
}
