package com.easypost.easyvcr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexCensorElement extends CensorElement {
    /**
     * Constructor.
     *
     * @param pattern       The regular expression pattern of the element to censor.
     * @param caseSensitive Whether the pattern is case sensitive.
     */
    public RegexCensorElement(String pattern, boolean caseSensitive) {
        super(pattern, caseSensitive);
    }

    /**
     * Replace the provided value with the provided replacement if the value matches the pattern.
     *
     * @param value       Value to apply the replacement to.
     * @param replacement Replacement for a detected matching section.
     * @return The replacement if the value matches the pattern, otherwise the original value.
     */
    public String matchAndReplaceAsNeeded(String value, String replacement) {
        Pattern pattern = Pattern.compile(this.value, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        return matcher.replaceAll(replacement);
    }

    /**
     * Return whether the provided element matches the name, accounting for case sensitivity.
     *
     * @param key The value to check.
     * @return True if the element matches the pattern.
     */
    @Override
    public boolean matches(String key) {
        Pattern pattern = Pattern.compile(this.value, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(key);

        // a portion of the key matches the pattern, find() == true, matches() == false (whole key must match)
        return matcher.find();
    }
}
