package com.wuyi.repairer.builder.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.wuyi.repairer.builder.Context
import com.wuyi.repairer.builder.tasks.tansform.FixTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Apply this plugin in the fix project, generate the fix hot-pluged .dex
 */
class FixPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        // now let's fix!
        Context.of().project = project

        AppPlugin agp = project.plugins.findPlugin("com.android.application")
        AppExtension appExtension = project.extensions.getByType(AppExtension)
        if (!agp || !appExtension) {
            throw new IllegalStateException("Repairer must used in a android project.\n" +
                    "Please make sure 'apply 'com.android.application'' above 'apply 'com.wuyi.repairer.builder''")
        }

        appExtension.registerTransform(new FixTransform())
    }
}