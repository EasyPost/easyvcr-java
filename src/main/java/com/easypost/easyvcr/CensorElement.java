package com.easypost.easyvcr;

public class CensorElement {
    /**
     * The name of the element to censor.
     */
    private final String name;
    /**
     * Whether the name must match exactly to trigger a censor.
     */
    private final boolean caseSensitive;

    /**
     * Constructor.
     * @param name The name of the element to censor.
     * @param caseSensitive Whether the name must match exactly to trigger a censor.
     */
    public CensorElement(String name, boolean caseSensitive) {
        this.name = name;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Return whether the element matches the name, accounting for case sensitivity.
     * @param key The name to check.
     * @return True if the element matches the name.
     */
    public boolean matches(String key) {
        if (caseSensitive) {
            return key.equals(name);
        } else {
            return key.equalsIgnoreCase(name);
        }
    }
}
