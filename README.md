## Application

This application will serve as Mission Control Dashboard backend for openclaw task prioritization/completion

## Mission Control

### Agents
There will be multiple autonomous agents of different disciplines that will retrieve and complete tasks

### Projects
There will be many concurrent projects. An agent can be assigned to many tasks of different projects

### Tasks
Tasks are an individual unit of work to be completed. They can be estimated/moved between statuses, approved, and ultimately completed

### Statuses
Backlog: These are tasks that are unstarted that may or may not be defined. The product managers will fill details to these tasks
Ready: These tasks have full details of what needs to be completed and are ready to be worked
In Progress: These tasks are in progress by the assign user
Blocked: These tasks cannot be completed due to a blocker the assigned user needs cleared.
In Review: These tasks have been completed by the assigned user and are awaiting approval
Done: These tasks have been completed

### User
Users can be viewers or "doers". The primary goal is to assign users to a task. Users will have their own unique token to view the system from their vantage point
