package io.pace.backend.domain.enums;

import java.util.Arrays;

public enum AccountStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(2),
    VERIFIED(3),
    ACTIVATE(4),
    DEACTIVATE(5),
    FORGOT_PASSWORD(6);

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
