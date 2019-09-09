package org.ib.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/5/13
 */
public class LanguageUtils {
    public static final Locale NONE = Locale.forLanguageTag("none");
    public static final int IDX_NONE = 0;
    //-- Locale Code
    private static final Map<Integer, Locale> codeToLocale = new HashMap<Integer, Locale>();
    private static final Map<Locale, Integer> localeToCode = new HashMap<Locale, Integer>();

    private static void addLanguageCode(int code, String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag);
        codeToLocale.put(code, locale);
        localeToCode.put(locale, code);
    }

    public static Locale getLocaleByCode(int code) {
        if (codeToLocale.containsKey(code)) {
            return codeToLocale.get(code);
        } else {
            return NONE;
        }
    }

    public static int getLanguageCodeByLocale(Locale locale) {
        if (localeToCode.containsKey(locale)) {
            return localeToCode.get(locale);
        } else {
            return IDX_NONE;
        }
    }

    public static Locale getLanguage(String code) {
        if (code == null || code.trim().length() == 0) {
            return Locale.US;
        } else {
            for (Locale language : codeToLocale.values()) {
                if (code.trim().equals(language.toLanguageTag())) {
                    return language;
                }
            }
            return Locale.US;
        }
    }

    static {
        addLanguageCode(IDX_NONE, NONE.toLanguageTag());
        addLanguageCode(1, "ar-JO");
        addLanguageCode(2, "ar-LB");
        addLanguageCode(3, "ar-QA");
        addLanguageCode(4, "ar-AE");
        addLanguageCode(5, "ar-MA");
        addLanguageCode(6, "ar-IQ");
        addLanguageCode(7, "ar-DZ");
        addLanguageCode(8, "ar-BH");
        addLanguageCode(9, "ar-LY");
        addLanguageCode(10, "ar-OM");
        addLanguageCode(11, "ar-SA");
        addLanguageCode(12, "ar-TN");
        addLanguageCode(13, "ar-YE");
        addLanguageCode(14, "eu");
        addLanguageCode(15, "ca");
        addLanguageCode(16, "cs");
        addLanguageCode(17, "nl-NL");
        addLanguageCode(18, "en-AU");
        addLanguageCode(19, "en-CA");
        addLanguageCode(20, "en-IN");
        addLanguageCode(21, "en-NZ");
        addLanguageCode(22, "en-ZA");
        addLanguageCode(23, "en-GB");
        addLanguageCode(24, "en-US");
        addLanguageCode(25, "fi");
        addLanguageCode(26, "fr-FR");
        addLanguageCode(27, "gl");
        addLanguageCode(28, "de-DE");
        addLanguageCode(29, "he");
        addLanguageCode(30, "hu");
        addLanguageCode(31, "is");
        addLanguageCode(32, "it-IT");
        addLanguageCode(33, "id");
        addLanguageCode(34, "ja");
        addLanguageCode(35, "ko");
        addLanguageCode(36, "la");
        addLanguageCode(37, "zh-CN");
        addLanguageCode(38, "zh-TW");
        addLanguageCode(39, "zh-HK");
        addLanguageCode(40, "yue");
        addLanguageCode(41, "ms-MY");
        addLanguageCode(42, "no-NO");
        addLanguageCode(43, "pl");
        addLanguageCode(44, "xx-piglatin");
        addLanguageCode(45, "pt-PT");
        addLanguageCode(46, "pt-BR");
        addLanguageCode(47, "ro-RO");
        addLanguageCode(48, "ru");
        addLanguageCode(49, "sr-SP");
        addLanguageCode(50, "sk");
        addLanguageCode(51, "es-AR");
        addLanguageCode(52, "es-BO");
        addLanguageCode(53, "es-CL");
        addLanguageCode(54, "es-CO");
        addLanguageCode(55, "es-CR");
        addLanguageCode(56, "es-DO");
        addLanguageCode(57, "es-EC");
        addLanguageCode(58, "es-SV");
        addLanguageCode(59, "es-GT");
        addLanguageCode(60, "es-HN");
        addLanguageCode(61, "es-MX");
        addLanguageCode(62, "es-NI");
        addLanguageCode(63, "es-PA");
        addLanguageCode(64, "es-PY");
        addLanguageCode(65, "es-PE");
        addLanguageCode(66, "es-PR");
        addLanguageCode(67, "es-ES");
        addLanguageCode(68, "es-US");
        addLanguageCode(69, "es-UY");
        addLanguageCode(70, "es-VE");
        addLanguageCode(71, "sv-SE");
        addLanguageCode(72, "tr");
        addLanguageCode(73, "zu");
    }
}
