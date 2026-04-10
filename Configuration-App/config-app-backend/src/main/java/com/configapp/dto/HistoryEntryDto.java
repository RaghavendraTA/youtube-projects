package com.configapp.dto;

import java.time.LocalDateTime;

public class HistoryEntryDto {
    private Long version;
    private LocalDateTime lastModifiedDate;
    private Boolean active;

    public HistoryEntryDto() {}

    public HistoryEntryDto(Long version, LocalDateTime lastModifiedDate, Boolean active) {
        this.version = version;
        this.lastModifiedDate = lastModifiedDate;
        this.active = active;
    }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long version;
        private LocalDateTime lastModifiedDate;
        private Boolean active;

        public Builder version(Long version) { this.version = version; return this; }
        public Builder lastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }

        public HistoryEntryDto build() {
            return new HistoryEntryDto(version, lastModifiedDate, active);
        }
    }
}
