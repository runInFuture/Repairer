package com.wuyi.repairer.builder;

import org.gradle.api.Project;

/**
 * The repairer build context, is singleton in a gradle build process.
 */
public class Context {
    private final static Context instance = new Context();

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public static Context of() {
        return instance;
    }
}
