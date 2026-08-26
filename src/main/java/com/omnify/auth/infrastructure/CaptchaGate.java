package com.omnify.auth.infrastructure;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;

//Quan li so lan auth fail theo IP va scope (login/register) de xac dinh khi nao can CAPTCHA
@Component
public class CaptchaGate {

    private static final String KEY_PREFIX = "captcha_gate:";
    private final RedissonClient redissonClient;
    private final CaptchaGateProperties captchaGateProperties;

    public CaptchaGate(RedissonClient redissonClient, CaptchaGateProperties captchaGateProperties) {
        this.redissonClient = redissonClient;
        this.captchaGateProperties = captchaGateProperties;
    }

    //kiem tra xem IP da vuot nguong fail va can CAPTCHA chua
    //true la can captcha false la ko can
    public boolean isCaptchaRequired(String scope, String ipAddress) {
        RAtomicLong counter = redissonClient.getAtomicLong(buildKey(scope, ipAddress));
        return counter.isExists() && counter.get() >= captchaGateProperties.getThreshold();
    }

    //Tang so lan fail va bat dau TTL tu lan fail dau tien
    //Goi khi login/register that bai de tang so lan fail va bat dau TTL neu la lan fail dau tien
    public void recordFailure(String scope, String ipAddress) {
        RAtomicLong counter = redissonClient.getAtomicLong(buildKey(scope, ipAddress));
        long current = counter.incrementAndGet();
        // Set thoi gian song cho counter trong redis
        if (current == 1) {
            counter.expire(Duration.ofSeconds(captchaGateProperties.getWindowSeconds()));
        }
    }

    //xoa counter khoi redis khi auth thanh cong
    //goi khi login/register thanh cong de xoa counter fail cua IP
    public void resetAttempts(String scope, String ipAddress) {
        redissonClient.getAtomicLong(buildKey(scope, ipAddress)).delete();
    }
    // Tao redis key theo scope (login/register) va IP
    private String buildKey(String scope, String ipAddress) {
        return KEY_PREFIX + scope + ":" + ipAddress;
    }
}
