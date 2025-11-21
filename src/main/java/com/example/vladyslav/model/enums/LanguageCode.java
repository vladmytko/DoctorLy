package com.example.vladyslav.model.enums;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public enum LanguageCode {
    en("English"),
    de("German"),
    uk("Ukrainian"),
    fr("French"),
    es("Spanish"),
    it("Italian"),
    pl("Polish"),
    bl("Bulgarian"),
    ar("Arabic"),
    zh("Chinese"),
    hi("Hindi");

    private final String label;

    private String code() { return name(); }
    private String label() { return label; }

    // ------- Lookup helpers ------
    private static final Map<String, LanguageCode> BY_CODE = Collections.unmodifiableMap(initByCode());

    private static Map<String, LanguageCode> initByCode() {
        Map<String, LanguageCode> m = new HashMap<>();
        for (LanguageCode lc : values()) m.put(lc.name(), lc);
        return m;
    }

    public static boolean isValidCode(String code) {
        if (code == null) return false;
        return BY_CODE.containsKey(code.toLowerCase());
    }

    public static Optional<LanguageCode> fromCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(BY_CODE.get(code.toLowerCase()));
    }

    /** Optional: map common free-text inputs to codes, for defensive handling. */
    private static final Map<String,String> ALIASES = Map.ofEntries(
            Map.entry("english","en"), Map.entry("eng","en"), Map.entry("🇬🇧","en"),
            Map.entry("german","de"),  Map.entry("deu","de"), Map.entry("🇩🇪","de"),
            Map.entry("ukrainian","uk"), Map.entry("ua","uk"), Map.entry("🇺🇦","uk"),
            Map.entry("french","fr"), Map.entry("español","es"), Map.entry("spanish","es"),
            Map.entry("italian","it"), Map.entry("polish","pl"), Map.entry("russian","ru"),
            Map.entry("arabic","ar"), Map.entry("chinese","zh"), Map.entry("hindi","hi")
    );

    /** Normalize any string to a valid code if possible. */
    public static Optional<String> normalizeToCode(String input) {
        if (input == null) return Optional.empty();
        String s = input.trim().toLowerCase();
        if (s.isEmpty()) return Optional.empty();
        // already a known code?
        if (isValidCode(s)) return Optional.of(s);
        // alias?
        String mapped = ALIASES.get(s);
        return mapped != null ? Optional.of(mapped) : Optional.empty();
    }

}
