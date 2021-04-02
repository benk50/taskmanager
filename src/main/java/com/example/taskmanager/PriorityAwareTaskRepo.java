package com.example.taskmanager;

import com.google.common.base.MoreObjects;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PriorityAwareTaskRepo implements TaskRepo {

    private final TaskFactory taskFactory;
    private final Long maxTasks;
    private final ConcurrentMap<Long, Task> tasksByPid = new ConcurrentHashMap<>();
    private final TreeMap<Priority, LinkedList<Task>> tasksByPriorityThenAge = new TreeMap<>();

    public PriorityAwareTaskRepo(final TaskFactory taskFactory, @Value("${max.tasks}") final Long maxTasks) {
        this.taskFactory = taskFactory;
        this.maxTasks = maxTasks;
    }

    @Override public Long add(final Priority newTaskPriority) {

        synchronized (tasksByPid) {
            if (tasksByPid.size() == maxTasks) {
                if (tasksByPriorityThenAge.lastKey().ordinal() < newTaskPriority.ordinal()) {

                    // new task has higher prio than any others, so remove oldest task from lowest prio
                    final Map.Entry<Priority, LinkedList<Task>> lowestPriorityTasks = tasksByPriorityThenAge.firstEntry();
                    final LinkedList<Task> value = lowestPriorityTasks.getValue();
                    final Task oldestTask = value.poll();
                    tasksByPid.remove(oldestTask.getPid());
                } else {
                    return -1L;
                }
            }

            final Task newTask = taskFactory.createTask(newTaskPriority);
            final Long newTaskPid = newTask.getPid();
            tasksByPid.put(newTaskPid, newTask);

            LinkedList<Task> tasksForThisPriority = tasksByPriorityThenAge.get(newTaskPriority);
            if (tasksForThisPriority == null) {
                tasksForThisPriority = new LinkedList<>();
                tasksByPriorityThenAge.put(newTaskPriority, tasksForThisPriority);
            }
            tasksForThisPriority.add(newTask);

            return newTaskPid;
        }
    }

    @Override public void remove(final Long pidOfTaskToRemove) {
        synchronized (tasksByPid) {
            final Task removed = tasksByPid.remove(pidOfTaskToRemove);
            if (removed != null) {
                final LinkedList<Task> tasks = tasksByPriorityThenAge.get(removed.getPriority());
                tasks.remove(removed);
            }
        }
    }

    @Override public void removeAllWith(final Priority priority) {
        synchronized (tasksByPid) {
            final LinkedList<Task> allTasksToRemove = tasksByPriorityThenAge.remove(priority);
            if (allTasksToRemove != null) {
                for (final Task task : allTasksToRemove) {
                    tasksByPid.remove(task.getPid());
                }
            }
        }
    }

    @Override public void removeAll() {
        synchronized (tasksByPid) {
            tasksByPid.clear();
            tasksByPriorityThenAge.clear();
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
