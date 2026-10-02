package uz.aifu.lms.coding.app;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.coding.domain.ProgrammingLanguage;
import uz.aifu.lms.coding.persistence.ProgrammingLanguageRepository;
import uz.aifu.lms.common.error.ErrorCode;
import uz.aifu.lms.common.error.Exceptions;

/** The languages code can be written and run in (product spec S-05.2: Python, Java, C++). */
@Service
public class ProgrammingLanguageService {

    private final ProgrammingLanguageRepository languages;

    public ProgrammingLanguageService(ProgrammingLanguageRepository languages) {
        this.languages = languages;
    }

    @Transactional(readOnly = true)
    public List<ProgrammingLanguage> listEnabled() {
        return languages.findByEnabledTrue().stream()
                .sorted(Comparator.comparing(ProgrammingLanguage::getDisplayName))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgrammingLanguage requireEnabled(String code) {
        return languages
                .findById(code == null ? "" : code)
                .filter(ProgrammingLanguage::isEnabled)
                .orElseThrow(
                        () ->
                                Exceptions.rule(
                                        ErrorCode.LANGUAGE_NOT_ENABLED, code + " is not enabled"));
    }
}
