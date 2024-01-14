package dk.mada.style;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import dk.mada.style.format.FormatterConfig;

/**
 * Extension for the Mada Style plugin.
 */
public abstract class MadaStylePluginExtension {
    public abstract Property<Boolean> getNullcheckingEnabled();
    @Nested
    public abstract FormatterConfig getFormatter();
    
    public void formatter(Action<? super FormatterConfig> action) {
        action.execute(getFormatter());
    }
}
