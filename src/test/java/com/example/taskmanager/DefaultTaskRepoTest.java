package com.example.taskmanager;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTaskRepoTest {

    private static final Instant FIXED_POINT_IN_TIME = Instant.now();

    private DefaultTaskRepo underTest;

    @Test
    public void addedTasksRetrievedCorrectly() {

        underTest = new DefaultTaskRepo(new DefaultTaskFactory(new PidProvider(), Clock.fixed(FIXED_POINT_IN_TIME, ZoneId.systemDefault())), 10L);

        final Priority expectedPriority1 = Priority.LOW;
        final Long expectedPid1 = underTest.add(expectedPriority1);

        final Priority expectedPriority2 = Priority.HI;
        final Long expectedPid2 = underTest.add(expectedPriority2);

        final List<Task> tasks = underTest.getAll();

        assertThat(tasks).hasSize(2);
        assertTaskAttributes(expectedPriority1, expectedPid1, tasks.get(0));
        assertTaskAttributes(expectedPriority2, expectedPid2, tasks.get(1));
    }

    private void assertTaskAttributes(final Priority expectedPriority2, final Long expectedPid2, final Task actual2) {
        assertThat(actual2.getPriority()).isEqualTo(expectedPriority2);
        assertThat(actual2.getPid()).isEqualTo(expectedPid2);
        assertThat(actual2.getCreationInstant()).isEqualTo(FIXED_POINT_IN_TIME);
    }

    @Test
    public void minusOneReturnedAndNothingAddedIfAlreadyAtMaxTasks() {
        underTest = new DefaultTaskRepo(new DefaultTaskFactory(new PidProvider(), Clock.fixed(FIXED_POINT_IN_TIME, ZoneId.systemDefault())), 1L);

        final Priority expectedPriority1 = Priority.LOW;
        underTest.add(expectedPriority1);
        assertThat(underTest.getAll()).hasSize(1);
        assertThat(underTest.add(Priority.HI)).isEqualTo(-1L);
        assertThat(underTest.getAll()).hasSize(1);
    }
}