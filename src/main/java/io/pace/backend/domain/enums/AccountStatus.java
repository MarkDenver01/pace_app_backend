package io.pace.backend.domain.enums;

import java.util.Arrays;

public enum AccountStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    private final int code;

    AccountStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AccountStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid AccountStatus code: " + code));
    }
}
