package com.example.taskmanager;

public interface TaskFactory {

    Task createTask(final Priority newTaskPriority);
}
