package com.configapp.dto;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private AdminDto user;

    public LoginResponse() {}

    public LoginResponse(String token, String refreshToken, Long expiresIn, AdminDto user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public AdminDto getUser() { return user; }
    public void setUser(AdminDto user) { this.user = user; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private AdminDto user;

        public Builder token(String token) { this.token = token; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder expiresIn(Long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder user(AdminDto user) { this.user = user; return this; }

        public LoginResponse build() {
            return new LoginResponse(token, refreshToken, expiresIn, user);
        }
    }
}
