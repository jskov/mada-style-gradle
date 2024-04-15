package dk.mada.buildinfo;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.TaskAction;

public class GenerateBuildInfo extends DefaultTask {
    private final Logger logger;

    public GenerateBuildInfo() {
        dependsOn("publish");
        
        this.logger = getProject().getLogger();
    }
    
    @TaskAction
    public void go() {
        logger.lifecycle(" RUN TASK");
        
        PublishingExtension pubs = getProject().getExtensions().getByType(PublishingExtension.class);
        PublicationContainer publications = pubs.getPublications();
        logger.lifecycle(" publications: {}", publications);
        publications.forEach(p -> {
            logger.lifecycle(" x: {} {}", p.getName(), p);
            if (p instanceof MavenPublication mp) {
                logger.lifecycle("  {}:{}", mp.getGroupId(), mp.getArtifactId());
                mp.getArtifacts().forEach(ma -> {
                    logger.lifecycle("  ma:{}", ma.getFile());
                    
                });
                logger.lifecycle("  {}", mp.getPom());
            }
        });
    }
}
