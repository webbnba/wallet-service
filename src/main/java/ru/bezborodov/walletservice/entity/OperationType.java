package ru.bezborodov.walletservice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {
    DEPOSIT("deposit"),
    WITHDRAW("withdraw");

    private final String operationType;

    OperationType(String operationType) {
        this.operationType = operationType;
    }

    @JsonValue
    public String getOperationType() {
        return operationType;
    }

    @JsonCreator
    public static OperationType fromValue(String value) {
        for (OperationType type : values()) {
            String currentType = type.getOperationType();
            if (currentType.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid value for Operation type Enum: " + value);
    }
}
