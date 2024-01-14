package dk.mada.style.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;

public class SpotlessConfigurator {
    private final FormatterConfig config;
    private final File gradleHomeDir;
    
    public SpotlessConfigurator(Logger logger, File gradleHomeDir, Properties dataChecksums, FormatterConfig config) {
        this.config = config;
        this.gradleHomeDir = gradleHomeDir;

        logger.lifecycle("See {}", config);
        
        getLocalConfigFile(gradleHomeDir, dataChecksums, "spotless/eclipse-formatter-mada.xml");
    }

    private Path getLocalConfigFile(File gradleHomeDir, Properties dataChecksums, String name) {
        Path madaConfigDir = gradleHomeDir.toPath().resolve("mada-data");
        try {
            String value = dataChecksums.getProperty(name);
            if (value == null) {
                throw new IllegalStateException("Failed to read " + name + " from data checksums: " + dataChecksums);
            }
    
            
            Path targetFile = madaConfigDir.resolve(name.replace('/', ':') + "-" + value);
            Path markerFile = madaConfigDir.resolve(targetFile.getFileName().toString() + ".valid");
            if (Files.exists(markerFile)) {
                return targetFile;
            }
            Files.createDirectories(madaConfigDir);
            Files.deleteIfExists(targetFile);

            String resourcePath = "/config/" + name;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IllegalStateException("Failed to read config file " + resourcePath);
                }
                Files.copy(is, targetFile);
                Files.createFile(markerFile);
                System.out.println("XXXXXX created " + targetFile);
            }
            
            return targetFile;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy config file " + name + " to " + madaConfigDir, e);
        }
    }

    public void configure(SpotlessExtension se) {
        se.java(this::configureJava);
    }

    private void configureJava(JavaExtension je) {
        je.target(config.getInclude());
        je.targetExclude(config.getExclude());
        je.formatAnnotations();
        
        RegularFile eclipseConfig = config.getEclipseConfig().getOrNull();
        if (eclipseConfig == null) {
            
        }
        
        je.eclipse().configFile(null);
    }
}
