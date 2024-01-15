package dk.mada.style.format;

import java.nio.file.Path;

import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;

import dk.mada.style.config.ConfigFileExtractor;

public class SpotlessConfigurator {
    private final Logger logger;
    private final FormatterConfig config;
    private final Path defaultConfigFile;
    
    public SpotlessConfigurator(Logger logger, ConfigFileExtractor configExtractor, FormatterConfig config) {
        this.logger = logger;
        this.config = config;

        defaultConfigFile = configExtractor.getLocalConfigFile("spotless/eclipse-formatter-mada.xml");
    }

    public void configure(SpotlessExtension se) {
        se.java(this::configureJava);
    }

    private void configureJava(JavaExtension je) {
        je.target(config.getInclude());
        je.targetExclude(config.getExclude());
        je.formatAnnotations();
        
        je.eclipse().configFile(getActiveConfigfile());
    }

    private Path getActiveConfigfile() {
        Path useConfig;
        RegularFile eclipseConfig = config.getEclipseConfig().getOrNull();
        if (eclipseConfig == null) {
            useConfig = defaultConfigFile;
        } else {
            useConfig = eclipseConfig.getAsFile().toPath();
        }
        logger.lifecycle("Use spotless config: {}", useConfig);
        return useConfig;
    }
}
