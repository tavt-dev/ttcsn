package com.friendify.app.interaction.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class UnavailablePostQueryAdapterTests {

    @Test
    void existsFailsFastUntilPostModuleIsMigrated() {
        UnavailablePostQueryAdapter adapter = new UnavailablePostQueryAdapter();

        assertThatThrownBy(() -> adapter.exists("post-1"))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.POST_MODULE_NOT_MIGRATED);
    }
}
