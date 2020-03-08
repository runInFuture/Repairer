package com.wuyi.repairer.builder.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class InstrumentTask extends DefaultTask {
    public final static String TASK_NAME = "repairerInstrumentAllClass";

    @Override
    public String getName() {
        return TASK_NAME;
    }

    /**
     * In gradle env, custom task should not new by hand.
     */
    @Inject
    public InstrumentTask() {
        config();
    }

    private void config() {
        List<Action> actions = collectActions();
        for (Action action : actions) {
            doLast(action.getDisplayName(), action);
        }
    }

    private List<Action> collectActions() {
        return Arrays.asList(new Action() {
            @Override
            public String getDisplayName() {
                return "action1";
            }

            @Override
            public void execute(Task task) {
                System.out.println("-----------------");
            }
        },new Action() {
            @Override
            public String getDisplayName() {
                return "acton2";
            }

            @Override
            public void execute(Task task) {
                System.out.println("++++++");
            }
        });
    }

}
