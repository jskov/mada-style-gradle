package dk.mada.style.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Properties;

import org.gradle.api.logging.Logger;

/**
 * Provides configuration resources as files.
 *
 * The files are stored in the Gradle home folder, indexed by a checksum. The checksum is used to check existence of
 * cached resources, before making an expensive operation.
 *
 * This class can handle two input types:
 *
 * * classpath resources
 * 
 * The checksum used is computed by the build process, made available in the resource CHECKSUMS_PROPERTIES.
 *
 * * local path/url
 *
 * A local file is provided as is. A URL is cached (indexed by the checksum of the URL).
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
     * @param logger        the gradle logger
     * @param gradleHomeDir the gradle home dir
     */
    public ConfigFileExtractor(Logger logger, Path gradleHomeDir) {
        this.logger = logger;
        this.gradleHomeDir = gradleHomeDir;

        this.dataChecksums = ResourceConfigProperties.readConfigProperties(CHECKSUMS_PROPERTIES);
    }

    /**
     * {@return a local file for the given configuration resource path}
     *
     * @param path the resource path
     */
    public Path getLocalConfigFileFromResource(String path) {
        String checksum = dataChecksums.getProperty(path);
        if (checksum == null) {
            throw new IllegalStateException("Failed to read " + path + " from data checksums: " + dataChecksums);
        }

        String resourcePath = "/config/" + path;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Failed to read config file " + resourcePath);
            }
            String txt = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return storeLocalFile(resourcePath, checksum, txt);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config file " + path, e);
        }
    }

    /**
     * Provides a local file from a configuration path.
     *
     * If the path starts with http:// or https:// it is treated as a remote file that gets downloaded and stored locally,
     * indexed by the url. So changing the content of the remote file will not have an effect on the cached data.
     *
     * @param path the local path to a file, or an URL to a remote file
     * @return a local file reference
     */
    public Path getLocalFileFromConfigPath(String path) {
        logger.lifecycle("READ config path '{}'", path);
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return copyUrlToLocalFile(path);
        } else {
            Path f = Paths.get(path);
            if (Files.isRegularFile(f)) {
                return f;
            }
            throw new IllegalArgumentException("Provided path '" + path + "' is not a file");
        }
    }

    private Path copyUrlToLocalFile(String url) {
        try {
            logger.lifecycle("Get {}", url);
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();
            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            String text = response.body();

            String checksum = checksum(url);
            String safePath = url.replaceAll("[^a-zA-Z0-9.-]", "_");
            return storeLocalFile(safePath, checksum, text);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interruped while fetching remote file " + url, e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to fetch remote file " + url, e);
        }
    }

    private Path storeLocalFile(String path, String checksum, String text) {
        Path madaConfigDir = gradleHomeDir.resolve("mada-data");
        try {
            String suffix = path.substring(path.lastIndexOf('.'));
            String filename = path.replace('/', ':').replace(suffix, "") + "-" + checksum + suffix;

            Path targetFile = madaConfigDir.resolve(filename);
            Path markerFile = madaConfigDir.resolve(filename + ".valid");
            if (Files.exists(markerFile)) {
                logger.debug("Already have config file {} : {}", path, targetFile);
                return targetFile;
            }
            logger.debug("Missing config file {}, extracting {}", path, targetFile);

            Files.createDirectories(madaConfigDir);
            Files.deleteIfExists(targetFile);

            Files.writeString(targetFile, text);
            Files.createFile(markerFile);

            return targetFile;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save " + path + " in " + madaConfigDir, e);
        }
    }

    private String checksum(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to make checksum", e);
        }
    }
}
