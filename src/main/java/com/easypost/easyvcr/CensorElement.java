package com.easypost.easyvcr;

public class CensorElement {
    /**
     * The value of the element to censor.
     */
    protected final String value;
    /**
     * Whether the value must match exactly to trigger a censor.
     */
    protected final boolean caseSensitive;

    /**
     * Constructor.
     * @param name The name of the element to censor.
     * @param caseSensitive Whether the name must match exactly to trigger a censor.
     */
    public CensorElement(String name, boolean caseSensitive) {
        this.value = name;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Return whether the element matches the name, accounting for case sensitivity.
     * @param key The name to check.
     * @return True if the element matches the name.
     */
    public boolean matches(String key) {
        if (caseSensitive) {
            return key.equals(value);
        } else {
            return key.equalsIgnoreCase(value);
        }
    }
}
