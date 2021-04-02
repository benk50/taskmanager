package com.example.taskmanager;

import java.util.concurrent.atomic.AtomicLong;

public class PidProvider {
    private final AtomicLong counter = new AtomicLong();

    public Long getNextPid() {
        return counter.getAndIncrement();
    }
}
