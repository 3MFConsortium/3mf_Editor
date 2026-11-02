package com.qualitylogic.x3mf;

/** Validation policy, independent of whether the application is running its GUI or CLI. */
public enum ValidationConsumer {
    PRINTER, EDITOR;

    public static ValidationConsumer parse(String value) {
        if (value != null) for (ValidationConsumer consumer : values())
            if (consumer.setting().equalsIgnoreCase(value.trim())) return consumer;
        throw new IllegalArgumentException("validationConsumer must be 'printer' or 'editor'; got: " + value);
    }

    public String setting() { return name().toLowerCase(java.util.Locale.ROOT); }
}
