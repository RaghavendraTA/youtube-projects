package com.configapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password) {
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

        public RegisterRequest build() {
            return new RegisterRequest(username, password);
        }
    }
}
