package iuh.fit.se.phazelsound.config;

import iuh.fit.se.phazelsound.common.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = request.getRemoteAddr();

        String key = "RATELIMIT:" + rateLimit.key() + ":" + ipAddress;

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, rateLimit.period(), rateLimit.unit());
        }

        if (currentCount != null && currentCount > rateLimit.count()) {
            log.warn("IP {} bị chặn vì spam API: {}", ipAddress, rateLimit.key());
            throw new RuntimeException("Bạn thao tác quá nhanh! Vui lòng thử lại sau " + redisTemplate.getExpire(key) + " giây.");
        }
        return joinPoint.proceed();
    }
}