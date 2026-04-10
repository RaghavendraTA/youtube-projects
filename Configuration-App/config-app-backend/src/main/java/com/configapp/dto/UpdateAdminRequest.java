package com.configapp.dto;

public class UpdateAdminRequest {
    private String username;
    private Boolean active;

    public UpdateAdminRequest() {}

    public UpdateAdminRequest(String username, Boolean active) {
        this.username = username;
        this.active = active;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username;
        private Boolean active;

        public Builder username(String username) { this.username = username; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }

        public UpdateAdminRequest build() {
            return new UpdateAdminRequest(username, active);
        }
    }
}
