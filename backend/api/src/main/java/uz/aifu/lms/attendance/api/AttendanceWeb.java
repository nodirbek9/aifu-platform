package uz.aifu.lms.attendance.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.i18n.LocaleResolver;

/** Request helpers shared by the attendance controllers: locale and client IP. */
@Component
class AttendanceWeb {

    private final LocaleResolver localeResolver;

    AttendanceWeb(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    LocaleCode locale(String explicit, HttpServletRequest request) {
        return localeResolver.preferred(explicit, request, null, null);
    }

    static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return http.getRemoteAddr();
    }
}
