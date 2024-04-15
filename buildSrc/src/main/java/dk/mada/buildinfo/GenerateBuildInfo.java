package dk.mada.buildinfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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
            Files.writeString(outputFile, build(primaryPub));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String build(MavenPublication primaryPub) {
        Property<String> cloneConnection = getProject().getObjects().property(String.class);
        
        primaryPub.getPom().scm(mps -> {
            cloneConnection.set(mps.getDeveloperConnection());
        });
        
        return """
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
    }
}
