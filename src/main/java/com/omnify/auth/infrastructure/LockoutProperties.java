package com.omnify.auth.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
@Getter
@Setter
@ConfigurationProperties(prefix = "omnify.security.auth.lockout")
public class LockoutProperties {
    //gia tri mac dinh neu ko lay duoc thong tin scheduleseconds ben application.yml
    private List<Integer> scheduleSeconds = List.of(0, 0, 0, 30, 60, 120, 300, 600);

    // Trả về số giây cần khoá dựa theo tổng số lần fail hiện tại
    public int resolveLockSeconds(int failedCount) {
        if (scheduleSeconds.isEmpty() || failedCount <= 0) {
            return 0;
        }
        int index = Math.min(failedCount - 1, scheduleSeconds.size() - 1);
        return scheduleSeconds.get(index);
    }
}