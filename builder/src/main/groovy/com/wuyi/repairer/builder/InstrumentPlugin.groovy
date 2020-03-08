package com.wuyi.repairer.builder

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.wuyi.repairer.builder.tasks.InstrumentTask
import com.wuyi.repairer.builder.tasks.TaskDirector
import com.wuyi.repairer.builder.tasks.tansform.InstrumentTransform
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

        AppPlugin agp = project.plugins.findPlugin("com.android.application")
        AppExtension appExtension = project.extensions.getByType(AppExtension)
        if (!agp || !appExtension) {
            throw new IllegalStateException("Repairer must used in a android project.\n" +
                    "Please make sure 'apply 'com.android.application'' above 'apply 'com.wuyi.repairer.builder''")
        }

        TaskDirector director = TaskDirector.with(project)
        director.runAfter("mergeDexDebug", project.tasks.create(InstrumentTask.TASK_NAME, InstrumentTask))

        // register the transforms, retrieve and instrument all the class of application
        InstrumentTransform instrumentTransform = new InstrumentTransform()
        appExtension.registerTransform(instrumentTransform)
    }

}