package dk.mada.style;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import dk.mada.style.format.FormatterConfig;

/**
 * Extension for the dk.mada Style plugin.
 */
public abstract class MadaStylePluginExtension {
    /** {@return flag for enabling null-checking} */
    public abstract Property<Boolean> getNullcheckingEnabled();

    /** {@return the formatter configuration} */
    @Nested
    public abstract FormatterConfig getFormatter();

    /**
     * The DSL entry for configuration of the formatter.
     *
     * @param action the action to run with the formatter config
     */
    public void formatter(Action<? super FormatterConfig> action) {
        action.execute(getFormatter());
    }
}
