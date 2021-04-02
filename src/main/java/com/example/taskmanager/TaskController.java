package com.example.taskmanager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;

@RestController
//@RequestMapping("/tasks")
public class TaskController {

    private enum RepoType {
        DEFAULT {
            public TaskRepo createRepo(final TaskFactory taskFactory, final Long maxTasks) {
                return new DefaultTaskRepo(taskFactory, maxTasks);
            }
        }, FIFO {
            public TaskRepo createRepo(final TaskFactory taskFactory, final Long maxTasks) {
                return new FifoTaskRepo(taskFactory, maxTasks);
            }
        }, PRIORITY {
            public TaskRepo createRepo(final TaskFactory taskFactory, final Long maxTasks) {
                return new PriorityAwareTaskRepo(taskFactory, maxTasks);
            }
        };

        public abstract TaskRepo createRepo(final TaskFactory taskFactory, final Long maxTasks);
    }

    private final TaskRepo tasks;

    public TaskController(@Value("${behaviour.type:default}") String repoTypeName, @Value("${max.tasks:1}") Long maxTasks) {
        final RepoType repoType = RepoType.valueOf(repoTypeName.toUpperCase());
        final TaskFactory taskFactory = new DefaultTaskFactory(new PidProvider(), Clock.systemDefaultZone());
        tasks = repoType.createRepo(taskFactory, maxTasks);
    }

    @GetMapping("/")
    public String index() {
        return "Greetings from TaskManager! Repo is [" + tasks.getClass() + " / " + tasks + "]";
    }

    @GetMapping("/tasks")
    public List<Task> listAllTasks() {
        return tasks.getAll();
    }

    @PostMapping("/addTask")
    public Long newTask(@RequestParam String priority) {
        final Priority priorityEnum = getPriorityFromName(priority);
        final Long pidOrError = tasks.add(priorityEnum);
        if (pidOrError == -1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "TaskManager full, behaviour does not allow adding new task of this priority");
        }
        return pidOrError;
    }

    @DeleteMapping("/tasks/{identifier}")
    void deleteTaskByPid(@PathVariable String identifier) {
        try {
            final Long pidToDelete = Long.parseLong(identifier);
            tasks.remove(pidToDelete);
        } catch (NumberFormatException nfe) {
            tasks.removeAllWith(getPriorityFromName(identifier));
        }
    }

    @DeleteMapping("/tasks")
    void deleteAllTasks() {
        tasks.removeAll();
    }

    private Priority getPriorityFromName(@RequestParam final String priority) {
        final Priority priorityEnum;
        try {
            priorityEnum = Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "[" + priority + "] is invalid priority for task", e);
        }
        return priorityEnum;
    }
}