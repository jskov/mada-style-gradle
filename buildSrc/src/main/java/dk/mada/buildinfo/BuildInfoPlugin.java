package dk.mada.buildinfo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class BuildInfoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Logger logger = project.getLogger();
        
        logger.lifecycle(" on {}", project);
        
    }

}
