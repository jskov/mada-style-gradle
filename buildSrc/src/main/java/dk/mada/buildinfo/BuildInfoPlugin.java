package dk.mada.buildinfo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.TaskProvider;

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

        TaskProvider<GenerateBuildInfo> r = project.getTasks().register("generateBuildInfo", GenerateBuildInfo.class);
/*        
        project.afterEvaluate(pr -> {
            PublishingExtension pubs = project.getExtensions().getByType(PublishingExtension.class);
            PublicationContainer publications = pubs.getPublications();
            logger.lifecycle(" publications: {}", publications);
            publications.whenObjectAdded(x -> logger.lifecycle("LATER {}", x));
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
        });
        
 */
    }
}
