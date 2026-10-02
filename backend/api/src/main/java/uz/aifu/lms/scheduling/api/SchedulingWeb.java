package uz.aifu.lms.scheduling.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import uz.aifu.lms.common.i18n.LocaleCode;
import uz.aifu.lms.common.i18n.LocaleResolver;

/** Request helpers shared by the scheduling controllers: locale, client IP, the campus "today". */
@Component
class SchedulingWeb {

    /** AIFU is in Uzbekistan; "today" for a timetable is the campus date, not the server's. */
    static final ZoneId CAMPUS_ZONE = ZoneId.of("Asia/Tashkent");

    private final LocaleResolver localeResolver;
    private final Clock clock;

    SchedulingWeb(LocaleResolver localeResolver, Clock clock) {
        this.localeResolver = localeResolver;
        this.clock = clock;
    }

    LocaleCode locale(String explicit, HttpServletRequest request) {
        return localeResolver.preferred(explicit, request, null, null);
    }

    LocalDate today() {
        return LocalDate.now(clock.withZone(CAMPUS_ZONE));
    }

    LocalDate dateOrToday(LocalDate date) {
        return date == null ? today() : date;
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
