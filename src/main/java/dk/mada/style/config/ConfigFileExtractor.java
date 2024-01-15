package dk.mada.style.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.gradle.api.logging.Logger;

/**
 * Copies configuration resources into files.
 *
 * Some plugins/tasks need input in the form of a file instead of an InputStream.
 *
 * This extractor helps make local copies of those files in the Gradle home folder.
 * The configuration files are extracted named with their checksum to avoid extracting
 * more than one instance per (dk.mada.style-)plugin version.
 *
 * The checksum used is computed by the build process, made available in the resource
 * CHECKSUMS_PROPERTIES.
 */
public final class ConfigFileExtractor {
    /** The checksum properties resource path. */
    private static final String CHECKSUMS_PROPERTIES = "/config/datafile-checksums.properties";
    private final Logger logger;
    /** The gradle home dir. */
    private final Path gradleHomeDir;
    /** The parsed checksum properties. */
    private final Properties dataChecksums;

    /**
     * Constructs a new instance.
     *
     * @param logger the gradle logger
     * @param gradleHomeDir the gradle home dir
     */
    public ConfigFileExtractor(Logger logger, Path gradleHomeDir) {
        this.logger = logger;
        this.gradleHomeDir = gradleHomeDir;
        
        this.dataChecksums = readDatafileChecksums();
    }

    /**
     * {@return a local file for the given configuration resource path}
     *
     * @param path the resource path
     */
    public Path getLocalConfigFile(String path) {
        Path madaConfigDir = gradleHomeDir.resolve("mada-data");
        try {
            String value = dataChecksums.getProperty(path);
            if (value == null) {
                throw new IllegalStateException("Failed to read " + path + " from data checksums: " + dataChecksums);
            }
    
            String suffix = "." + path.replaceAll(".*[.]", "");
            String filename = path.replace('/', ':').replace(suffix, "") + "-" + value + suffix;

            Path targetFile = madaConfigDir.resolve(filename);
            Path markerFile = madaConfigDir.resolve(filename + ".valid");
            if (Files.exists(markerFile)) {
                logger.debug("Already have config file {} : {}", path, targetFile);
                return targetFile;
            }
            Files.createDirectories(madaConfigDir);
            Files.deleteIfExists(targetFile);

            logger.debug("Missing config file {}, extracting {}", path, targetFile);

            String resourcePath = "/config/" + path;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IllegalStateException("Failed to read config file " + resourcePath);
                }
                Files.copy(is, targetFile);
                Files.createFile(markerFile);
            }

            return targetFile;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy config file " + path + " to " + madaConfigDir, e);
        }
    }

    private static Properties readDatafileChecksums() {
        try (InputStream is = ConfigFileExtractor.class.getResourceAsStream(CHECKSUMS_PROPERTIES)) {
            if (is == null) {
                throw new IllegalStateException("Failed to find resource " + CHECKSUMS_PROPERTIES);
            }
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read properties from resource " + CHECKSUMS_PROPERTIES, e);
        }
    }
}
