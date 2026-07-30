package com.github.alantr7.commandhider.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CommandRuleSet {

    private static final String REGEX_PREFIX = "regex:";

    private final Set<String> literals = new LinkedHashSet<>();

    private final List<Pattern> patterns = new ArrayList<>();

    public void add(String rule) {
        String regexPattern = regexPattern(rule);
        if (regexPattern != null) {
            if (!regexPattern.isEmpty())
                patterns.add(Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE));
            return;
        }

        String normalized = normalize(rule);
        if (normalized.isEmpty())
            return;

        if (literals.add(normalized))
            patterns.add(Pattern.compile(toLiteralRegex(normalized), Pattern.CASE_INSENSITIVE));
    }

    public boolean matches(String command) {
        String normalized = normalize(command);
        if (literals.contains(normalized))
            return true;

        for (Pattern pattern : patterns) {
            if (pattern.matcher(normalized).matches())
                return true;
        }
        return false;
    }

    public Set<String> getLiterals() {
        return Collections.unmodifiableSet(literals);
    }

    public List<Pattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    public boolean isEmpty() {
        return literals.isEmpty() && patterns.isEmpty();
    }

    public static boolean isRegexRule(String rule) {
        return normalize(rule).startsWith(REGEX_PREFIX);
    }

    public static String normalize(String value) {
        if (value == null)
            return "";

        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static PatternSyntaxException compileError(String rule) {
        String pattern = regexPattern(rule);
        if (pattern == null)
            pattern = normalize(rule);

        try {
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            return null;
        } catch (PatternSyntaxException exception) {
            return exception;
        }
    }

    private static String regexPattern(String rule) {
        if (rule == null)
            return null;

        String trimmed = rule.trim();
        if (!trimmed.toLowerCase(Locale.ROOT).startsWith(REGEX_PREFIX))
            return null;

        return trimmed.substring(REGEX_PREFIX.length()).trim();
    }

    private static String toLiteralRegex(String rule) {
        return Pattern.quote(rule);
    }

}
