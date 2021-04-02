package com.example.taskmanager;

import java.util.List;

public interface TaskRepo {

    Long add(final Priority newTaskPriority);

    void removeAllWith(final Priority priority);

    void remove(final Long pidOfTaskToRemove);

    void removeAll();

    List<Task> getAll();
}