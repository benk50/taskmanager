package com.example.taskmanager;

import java.time.Clock;

public class DefaultTaskFactory implements TaskFactory {
    private final PidProvider pidProvider;
    private final Clock clock;

    public DefaultTaskFactory(final PidProvider pidProvider, final Clock clock) {
        this.pidProvider = pidProvider;
        this.clock = clock;
    }

    public Task createTask(final Priority newTaskPriority) {
        return new Task(pidProvider.getNextPid(), newTaskPriority, clock.instant());
    }
}
