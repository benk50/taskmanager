package com.example.taskmanager;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTaskRepo implements TaskRepo {

    private final TaskFactory taskFactory;
    private final Long maxTasks;
    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();

    public DefaultTaskRepo(final TaskFactory taskFactory, final Long maxTasks) {
        this.taskFactory = taskFactory;
        this.maxTasks = maxTasks;
    }

    @Override public Long add(final Priority newTaskPriority) {
        synchronized (tasks) {
            if (tasks.size() == maxTasks) {
                return -1L;
            }
            final Task newTask = taskFactory.createTask(newTaskPriority);
            final Long newPid = newTask.getPid();
            tasks.put(newPid, newTask);
            return newPid;
        }
    }

    @Override public void remove(final Long pidOfTaskToRemove) {
        tasks.remove(pidOfTaskToRemove);
    }

    @Override public void removeAllWith(final Priority priority) {
        for (final Map.Entry<Long, Task> taskEntry : tasks.entrySet()) {
            if (priority == taskEntry.getValue().getPriority()) {
                tasks.remove(taskEntry.getKey()); // don't need more checking as Task is immutable and mapping to pid doesn't change
            }
        }
    }

    @Override public void removeAll() {
        tasks.clear();
    }

    @Override public List<Task> getAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("maxTasks", maxTasks)
                .add("content", getAll())
                .toString();
    }
}
