package com.wuyi.repairer.builder.tasks;

import com.wuyi.repairer.builder.Logger;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaskDirector {
    private Project project;
    private Logger logger;
    private List<Runnable> configRunnables = new ArrayList<>();

    private TaskDirector(Project project) {
        this.project = project;
        logger = Logger.getInstance();

        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            public void graphPopulated(@NotNull TaskExecutionGraph graph) {
//                runConfig();
            }
        });
    }

    public static TaskDirector with(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("TaskDirector: project must not be null!");
        }
        return new TaskDirector(project);
    }

    public void runAfter(String anchorTaskName, Task task) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Task anchorTask = findTask(anchorTaskName);
                if (anchorTask == null) {
                    logger.log(String.format("TaskDirector: anchorTask[%s] not found!", anchorTaskName));
                }
                runAfter(anchorTask, task);
            }
        };
        r.run();
        configRunnables.add(r);
    }

    public void runAfter(Task anchorTask, Task task) {
//        task.dependsOn(anchorTask);
        if (anchorTask == null) return;
        anchorTask.dependsOn(task);
        Set<Object> dependsOn = anchorTask.getDependsOn();
        for (Object o : dependsOn) {
            logger.log("depend: " + o);
        }
    }

    public Task findTask(String taskName) {
        return project.getTasks().findByName(taskName);
    }

    private void runConfig() {
        for (Runnable r : configRunnables) {
            r.run();
        }
    }
}
