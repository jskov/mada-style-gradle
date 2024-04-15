package dk.mada.buildinfo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.logging.Logger;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent;

public class BuildInfoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Logger logger = project.getLogger();
        
        // may have to select "main" pom to pull info from
        project.afterEvaluate(p -> {
            p.getTasks().withType(GenerateMavenPom.class).configureEach(t -> {
                logger.lifecycle("config {}", t.getName());
                MavenPom pom = t.getPom();
                logger.lifecycle(" name: {}", pom.getName());
                logger.lifecycle(" dest: {}", t.getDestination());
            });
        });
        
        logger.lifecycle(" on {}", project);
        
        PublishingExtension pubs = project.getExtensions().getByType(PublishingExtension.class);
        
        PublicationContainer publications = pubs.getPublications();
        
        publications.forEach(p -> {
            logger.lifecycle(" x: {}", p.getName());
            
        });
        
        logger.lifecycle("confs: {}", project.getConfigurations().getNames());
        
        project.getConfigurations().getByName("archives").getAllArtifacts().forEach(pa -> {
            logger.lifecycle(" pa: {} : {}", pa.getName(), pa.getFile());
        });

        SoftwareComponent sc = project.getComponents().getByName("java");
        logger.lifecycle(" sc: {}", sc);
        DefaultJvmSoftwareComponent s = (DefaultJvmSoftwareComponent)sc;
        logger.lifecycle(" s: {}", s.getName());
        
    }

}
