package dk.mada.style.nullcheck;

import org.gradle.api.provider.Property;

/**
 * DSL container for null-checker configuration.
 */
public abstract class NullcheckerConfig {
    /** {@return a flag to control the activation of the null-checker} */
    public abstract Property<Boolean> getEnabled();

    /**
     * Creates a new instance with default settings.
     */
    public NullcheckerConfig() { // NOSONAR - must be public, or Groovy/Gradle does not see it
        getEnabled().set(true);
    }
}
