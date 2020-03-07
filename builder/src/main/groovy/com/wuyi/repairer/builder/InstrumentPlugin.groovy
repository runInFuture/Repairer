package com.wuyi.repairer.builder

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The entrance of repairer.
 * Instrument all the class in application, so we can fix them later if need.
 * Precisely, delegate the origin method call to the fixed method, if we had one.
 */
class InstrumentPlugin implements Plugin<Project> {
    Logger logger = Logger.getInstance()

    @Override
    void apply(Project project) {
        // now let's hook!

        // first of all, retrieve and instrument all the class of application
        ClassInstrumentor instrumentor = new ClassInstrumentor()
    }

}