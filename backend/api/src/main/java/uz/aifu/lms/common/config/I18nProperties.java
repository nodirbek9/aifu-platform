package uz.aifu.lms.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aifu.i18n")
public record I18nProperties(String defaultLocale, List<String> supportedLocales) {}
