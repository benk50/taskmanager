package com.example.taskmanager;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.Instant;

@EqualsAndHashCode @ToString
public class Task {

    @NonNull private final Long pid;
    @NonNull private final Priority priority;
    @NonNull private final Instant creationInstant;

    public Task(final Long pid, final Priority priority, final Instant creationInstant) {
        this.pid = pid;
        this.priority = priority;
        this.creationInstant = creationInstant;
    }

    public Long getPid() {
        return pid;
    }

    public Priority getPriority() {
        return priority;
    }

    public Instant getCreationInstant() {
        return creationInstant;
    }
}
