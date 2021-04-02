package com.example.taskmanager;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FifoTaskRepo implements TaskRepo {

    private final TaskFactory taskFactory;
    private final Long maxTasks;
    private final ConcurrentMap<Long, Task> tasksByPid = new ConcurrentHashMap<>();
    private final LinkedList<Task> tasksInInsertionOrder = new LinkedList<>();

    public FifoTaskRepo(final TaskFactory taskFactory, final Long maxTasks) {
        this.taskFactory = taskFactory;
        if (maxTasks < 1) {
            throw new IllegalArgumentException("Maximum tasks for TaskManager is [" + maxTasks + "], cannot be less than 1");
        }
        this.maxTasks = maxTasks;
    }

    @Override public Long add(final Priority newTaskPriority) {
        synchronized (tasksByPid) {
            if (tasksByPid.size() == maxTasks) {
                final Task removed = tasksInInsertionOrder.poll();
                tasksByPid.remove(removed.getPid());
            }

            final Task newTask = taskFactory.createTask(newTaskPriority);
            final Long newTaskPid = newTask.getPid();
            tasksByPid.put(newTaskPid, newTask);
            tasksInInsertionOrder.offer(newTask);
            return newTaskPid;
        }
    }

    @Override public void remove(final Long pidOfTaskToRemove) {
        synchronized (tasksByPid) {
            final Task removed = tasksByPid.remove(pidOfTaskToRemove);
            if (removed != null) {
                tasksInInsertionOrder.remove(removed);
            }
        }
    }

    @Override public void removeAllWith(final Priority priority) {
        synchronized (tasksByPid) {
            for (final Map.Entry<Long, Task> taskEntry : tasksByPid.entrySet()) {
                if (priority == taskEntry.getValue().getPriority()) {
                    remove(taskEntry.getKey()); // don't need more checking as Task is immutable and mapping to pid doesn't change
                }
            }
        }
    }

    @Override public void removeAll() {
        synchronized (tasksByPid) {
            tasksByPid.clear();
            tasksInInsertionOrder.clear();
        }
    }

    @Override public List<Task> getAll() {
        return new ArrayList<>(tasksByPid.values());
    }

    @Override public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("maxTasks", maxTasks)
                .add("content", getAll())
                .toString();
    }
}
