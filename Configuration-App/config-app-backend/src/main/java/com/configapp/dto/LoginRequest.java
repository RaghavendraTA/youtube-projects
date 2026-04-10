package com.configapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {
    @NotNull(message = "Username is required")
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be empty")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username;
        private String password;

        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }

        public LoginRequest build() {
            return new LoginRequest(username, password);
        }
    }
}
