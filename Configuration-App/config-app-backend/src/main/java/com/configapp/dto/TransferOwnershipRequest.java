package com.configapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferOwnershipRequest {
    @JsonProperty("transfer-to")
    private String transferTo;

    public TransferOwnershipRequest() {}

    public TransferOwnershipRequest(String transferTo) {
        this.transferTo = transferTo;
    }

    public String getTransferTo() { return transferTo; }
    public void setTransferTo(String transferTo) { this.transferTo = transferTo; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String transferTo;

        public Builder transferTo(String transferTo) { this.transferTo = transferTo; return this; }

        public TransferOwnershipRequest build() {
            return new TransferOwnershipRequest(transferTo);
        }
    }
}
