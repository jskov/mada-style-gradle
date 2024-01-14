package dk.mada.style;

import org.gradle.api.provider.Property;

/**
 * Extension for the Mada Style plugin.
 */
public interface MadaStylePluginExtension {
    Property<Boolean> getNullcheckingEnabled();
    Property<Boolean> getFormattingEnabled();
}
