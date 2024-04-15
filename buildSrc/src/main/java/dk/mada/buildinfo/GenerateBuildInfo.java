package dk.mada.buildinfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateBuildInfo extends DefaultTask {
    private static final String NL = System.lineSeparator();
    private final Logger logger;
    private final Project project;

    @OutputFile
    public abstract RegularFileProperty getBuildInfoFile();
    
    @Inject
    public GenerateBuildInfo(ProjectLayout layout) {
        dependsOn("publish");
        getOutputs().upToDateWhen(t -> false);
        project = getProject();
        this.logger = project.getLogger();
        
        getBuildInfoFile().convention(layout.getBuildDirectory().file("buildinfo/" +project.getName() + "-" + project.getVersion() + ".buildinfo"));
    }
    
    @TaskAction
    public void go() {
        Path outputFile = getBuildInfoFile().get().getAsFile().toPath();
        
        logger.lifecycle(" RUN TASK : {}", outputFile);

        PublishingExtension pubs = getProject().getExtensions().getByType(PublishingExtension.class);
        PublicationContainer publications = pubs.getPublications();
        logger.lifecycle(" publications: {}", publications);
        List<MavenPublication> mavenPublications = publications.stream()
            .filter(p -> p instanceof MavenPublication)
            .map(p -> MavenPublication.class.cast(p))
            .toList();

        if (mavenPublications.isEmpty()) {
            logger.warn("No maven publications to base buildinfo on");
            return;
        }
        
        MavenPublication primaryPub = mavenPublications.getFirst();
        MavenPom primaryPom = primaryPub.getPom();
        
//        publications.forEach(p -> {
//            logger.lifecycle(" x: {} {}", p.getName(), p);
//            if (p instanceof MavenPublication mp) {
//                logger.lifecycle("  {}:{}", mp.getGroupId(), mp.getArtifactId());
//                mp.getArtifacts().forEach(ma -> {
//                    logger.lifecycle("  ma:{}", ma.getFile());
//                    
//                });
//                logger.lifecycle("  {}", mp.getPom());
//            }
//        });

        try {
            Files.createDirectories(outputFile.getParent());
            Files.writeString(outputFile, build(primaryPub, mavenPublications));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String build(MavenPublication primaryPub, List<MavenPublication> publications) {
        Property<String> cloneConnection = getProject().getObjects().property(String.class);
        
        primaryPub.getPom().scm(mps -> {
            cloneConnection.set(mps.getDeveloperConnection());
        });
        
        String header = """
            buildinfo.version=1.0-SNAPSHOT
    
            name=@NAME@
            group-id=@GROUP@
            artifact-id=@ARTIFACT@
            version=@VERSION@

            build-tool=gradle

            java.version=@JAVA_VERSION@
            java.vendor=@JAVA_VENDOR@
            os.name=@OS_NAME@

            source.scm.uri=@GIT_URI@
            source.scm.tag=@VERSION@

            """
                .replace("@NAME@", project.getName())
                .replace("@GROUP@", Objects.toString(project.getGroup()))
                .replace("@ARTIFACT@", primaryPub.getArtifactId())
                .replace("@VERSION@", Objects.toString(project.getVersion()))
                .replace("@GIT_URI@", cloneConnection.get())
                .replace("@JAVA_VERSION@", System.getProperty("java.version"))
                .replace("@JAVA_VENDOR@", System.getProperty("java.vendor"))
                .replace("@OS_NAME@", System.getProperty("os.name"))
                ;
        
        String output = header;
        int publicationIx = 0;
        for (MavenPublication pub : publications) {
            output = output + "outputs." + publicationIx + ".coordinates=" + pub.getGroupId() + ":" + pub.getArtifactId() + NL;
            
            int artifactIx = 0;
            for (var art : pub.getArtifacts()) {
                String prefix = "outputs." + publicationIx + "." + artifactIx;
                output = output + prefix + ".filename=" + art.getFile().getName() + NL;
                output = output + prefix + ".length=" + art.getFile().length() + NL;
                output = output + prefix + ".checksums.sha512=" + sha512sum(art.getFile()) + NL;
                artifactIx++;
            }

            publicationIx++;
        }
        
        return output;
    }
    
    private String sha512sum(File file) {
        MessageDigest md;
        byte[] buffer = new byte[8192];
        try (InputStream is = Files.newInputStream(file.toPath())) {
            md = MessageDigest.getInstance("SHA-512");
            int read;
            while ((read = is.read(buffer)) > 0) {
                md.update(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to checksum file " + file, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to get digester for sha-512", e);
        }
        return HexFormat.of().formatHex(md.digest());
    }
}
