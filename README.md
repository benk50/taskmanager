# TaskManager

You can specify max tasks and 'adding behaviour' (options: `default`, `fifo`, `priority`) in the `src/main/resources/application.properties` file, then app can be run from project root using `.gradlew bootRun`.

I'm completely new to Spring and frontend development, and unfortunately wasn't able to get any UI for this working in the time I'd allotted. 

curl directions instead:

* Add a task (param options: 'low', 'med', 'hi'): `curl --data "priority={your_priority}" localhost:8080/addTask`
* List all tasks (no sort options): `curl localhost:8080/tasks`
* Delete by PID: `curl -X DELETE localhost:8080/tasks/{your_pid} -H 'Content-type:application/json'`
* Delete by priority: `curl -X DELETE localhost:8080/tasks/{your_priority} -H 'Content-type:application/json'`
* Delete all: `curl -X DELETE localhost:8080/tasks/ -H 'Content-type:application/json'`

Other TODOs:

* More test coverage
* Javadoc
* Refactor away some dupe code in the Repo hierarchy
* (Re)consider if the curent use of Strategy pattern for entire Repo object really optimizes for most frequent use cases

Thanks for your time!