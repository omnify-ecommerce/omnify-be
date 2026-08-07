package com.omnify.auth.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "omnify.security.auth.lockout")
public class LockoutProperties {

    // index 0 = sau lần fail thứ 1, index 1 = sau lần fail thứ 2, v.v.
    // fail thứ N vượt quá size của list -> dùng phần tử cuối (trần tối đa)
    private List<Integer> scheduleSeconds = List.of(0, 0, 0, 30, 60, 120, 300, 600);

    public List<Integer> getScheduleSeconds() {
        return scheduleSeconds;
    }

    public void setScheduleSeconds(List<Integer> scheduleSeconds) {
        this.scheduleSeconds = scheduleSeconds;
    }

    /** Trả về số giây cần khoá dựa theo tổng số lần fail hiện tại (đã cộng dồn). */
    public int resolveLockSeconds(int failedCount) {
        if (scheduleSeconds.isEmpty() || failedCount <= 0) {
            return 0;
        }
        int index = Math.min(failedCount - 1, scheduleSeconds.size() - 1);
        return scheduleSeconds.get(index);
    }
}