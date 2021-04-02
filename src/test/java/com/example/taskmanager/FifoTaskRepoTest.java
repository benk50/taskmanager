package com.example.taskmanager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FifoTaskRepoTest {

    private static final Instant NOW = Clock.systemDefaultZone().instant();

    private FifoTaskRepo underTest;

    @Test
    public void addedTasksBeforeLimitRetrievedCorrectly() {

        underTest = new FifoTaskRepo(new DefaultTaskFactory(new PidProvider(), Clock.fixed(NOW, ZoneId.systemDefault())), 10L);

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
        assertThat(actual2.getCreationInstant()).isEqualTo(NOW);
    }

    @Test
    public void taskAddedAndOldestTaskRemovedWhenAlreadyAtMaxTasks() {

        final PidProvider pidProvider = new PidProvider();
        final Task mediumTask = new Task(pidProvider.getNextPid(), Priority.MED, NOW);
        final Task olderLowTask = new Task(pidProvider.getNextPid(), Priority.LOW, NOW);
        final Task newerLowTask = new Task(pidProvider.getNextPid(), Priority.LOW, NOW);
        final Task hiTask = new Task(pidProvider.getNextPid(), Priority.HI, NOW);

        final TaskFactory mockTaskFactory = Mockito.mock(TaskFactory.class);
        Mockito.when(mockTaskFactory.createTask(Mockito.any(Priority.class)))
                .thenReturn(mediumTask, olderLowTask, newerLowTask, hiTask);

        underTest = new FifoTaskRepo(mockTaskFactory, 3L);
        underTest.add(Priority.MED);
        underTest.add(Priority.LOW);
        underTest.add(Priority.LOW);
        underTest.add(Priority.HI);

        assertThat(underTest.getAll()).containsExactlyInAnyOrder(olderLowTask, newerLowTask, hiTask);
    }
}