package dk.mada.style.nullcheck;

import org.gradle.api.provider.Property;

/**
 * DSL container for null-checker configuration.
 */
public abstract class NullcheckerConfig {
    /** {@return a comma-separated list of packages to check for null} */
    public abstract Property<String> getPackages();

    /** {@return a regular expression of exluded paths} */
    public abstract Property<String> getExcludedPathsRegexp();

    /**
     * Creates a new instance with default settings.
     */
    public NullcheckerConfig() { // NOSONAR - must be public, or Groovy/Gradle does not see it
        getPackages().convention("dk.mada, org");
    }
}
