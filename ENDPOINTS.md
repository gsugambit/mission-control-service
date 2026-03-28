# Mission Control Endpoints

This document lists the available REST endpoints and their data structures for the Mission Control Service.

## DTO Definitions

### UserDto
- `id`: UUID - Unique identifier for the user.
- `userName`: String - The user's name.
- `dateCreated`: Instant - Timestamp when the user was created.
- `dateModified`: Instant - Timestamp when the user was last modified.

### ProjectDto
- `id`: UUID - Unique identifier for the project.
- `name`: String - Name of the project.
- `description`: String - Detailed description of the project.
- `assignedUserId`: UUID - ID of the user assigned to this project.
- `status`: MissionStatus - Current status of the project.
- `blockedReason`: String - Reason why the project is blocked (if status is BLOCKED).
- `dateCreated`: Instant - Timestamp when the project was created.
- `dateModified`: Instant - Timestamp when the project was last modified.

### TaskDto
- `id`: UUID - Unique identifier for the task.
- `projectId`: UUID - ID of the project this task belongs to.
- `assignedUserId`: UUID - ID of the user assigned to this task.
- `status`: MissionStatus - Current status of the task.
- `blockedReason`: String - Reason why the task is blocked (if status is BLOCKED).
- `name`: String - Name of the task.
- `description`: String - Description of what needs to be done.
- `acceptanceCriteria`: String - Criteria for task completion.
- `dateCreated`: Instant - Timestamp when the task was created.
- `dateModified`: Instant - Timestamp when the task was last modified.

### ErrorResponseDto
- `status`: int - HTTP status code.
- `error`: String - Error type or summary.
- `message`: String - Detailed error message.
- `timestamp`: Instant - Timestamp when the error occurred.

### MissionStatus (Enum)
Values: `BACKLOG`, `READY`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`

---

## Users

Endpoints for managing users in the system.

### Create User
- **Path**: `POST /api/mission-control/v1/users`
- **Input**: `UserDto` (JSON)
    - Required: `userName`
- **Output**: `UserDto` (JSON)
- **Description**: Creates a new user in the system.

### Get All Users
- **Path**: `GET /api/mission-control/v1/users`
- **Input**: None
- **Output**: `List<UserDto>` (JSON)
- **Description**: Retrieves all users in the system.

### Delete User
- **Path**: `DELETE /api/mission-control/v1/users/{id}`
- **Input**: `id` (Path variable, UUID)
- **Output**: None (HTTP 204 No Content)
- **Description**: Deletes a user. Fails with 409 if the user is assigned to any project or task.

---

## Projects

Endpoints for managing projects and their lifecycle.

### Create Project
- **Path**: `POST /api/mission-control/v1/projects`
- **Input**: `ProjectDto` (JSON)
    - Required: `name`
    - Optional: `description`
- **Output**: `ProjectDto` (JSON)
- **Description**: Creates a new project.

### Get Project by ID
- **Path**: `GET /api/mission-control/v1/projects/{id}`
- **Input**: `id` (Path variable, UUID)
- **Output**: `ProjectDto` (JSON)
- **Description**: Retrieves details of a specific project.

### Get All Projects
- **Path**: `GET /api/mission-control/v1/projects`
- **Input**: None
- **Output**: `List<ProjectDto>` (JSON)
- **Description**: Retrieves all projects in the system.

### Update Project
- **Path**: `PUT /api/mission-control/v1/projects/{id}`
- **Input**: 
    - `id`: Path variable (UUID)
    - `ProjectDto`: Request body (JSON)
- **Output**: `ProjectDto` (JSON)
- **Description**: Updates an existing project's name and description.

### Delete Project
- **Path**: `DELETE /api/mission-control/v1/projects/{id}`
- **Input**: `id` (Path variable, UUID)
- **Output**: None (HTTP 204 No Content)
- **Description**: Deletes a project.

### Assign User to Project
- **Path**: `PATCH /api/mission-control/v1/projects/{id}/assign/{userId}`
- **Input**: 
    - `id`: Path variable (UUID)
    - `userId`: Path variable (UUID)
- **Output**: `ProjectDto` (JSON)
- **Description**: Assigns a user to a project.

### Update Project Status
- **Path**: `PATCH /api/mission-control/v1/projects/{id}/status/{status}`
- **Input**: 
    - `id`: Path variable (UUID)
    - `status`: Path variable (MissionStatus)
    - `ProjectDto`: Optional request body (JSON) to provide `blockedReason`
- **Output**: `ProjectDto` (JSON)
- **Description**: Updates the status of a project. If status is BLOCKED, the `blockedReason` can be provided in the request body.

### Get Projects by User
- **Path**: `GET /api/mission-control/v1/projects/user/{userId}`
- **Input**: `userId` (Path variable, UUID)
- **Output**: `List<ProjectDto>` (JSON)
- **Description**: Retrieves all projects assigned to a specific user.

---

## Tasks

Endpoints for managing tasks within projects.

### Create Task
- **Path**: `POST /api/mission-control/v1/tasks`
- **Input**: `TaskDto` (JSON)
    - Required: `projectId`, `name`, `description`
    - Optional: `acceptanceCriteria`
- **Output**: `TaskDto` (JSON)
- **Description**: Creates a new task within a project.

### Get Task by ID
- **Path**: `GET /api/mission-control/v1/tasks/{id}`
- **Input**: `id` (Path variable, UUID)
- **Output**: `TaskDto` (JSON)
- **Description**: Retrieves details of a specific task.

### Get All Tasks
- **Path**: `GET /api/mission-control/v1/tasks`
- **Input**: None
- **Output**: `List<TaskDto>` (JSON)
- **Description**: Retrieves all tasks in the system.

### Update Task
- **Path**: `PUT /api/mission-control/v1/tasks/{id}`
- **Input**: 
    - `id`: Path variable (UUID)
    - `TaskDto`: Request body (JSON)
- **Output**: `TaskDto` (JSON)
- **Description**: Updates an existing task's name, description and acceptance criteria.

### Delete Task
- **Path**: `DELETE /api/mission-control/v1/tasks/{id}`
- **Input**: `id` (Path variable, UUID)
- **Output**: None (HTTP 204 No Content)
- **Description**: Deletes a task.

### Assign User to Task
- **Path**: `PATCH /api/mission-control/v1/tasks/{id}/assign/{userId}`
- **Input**: 
    - `id`: Path variable (UUID)
    - `userId`: Path variable (UUID)
- **Output**: `TaskDto` (JSON)
- **Description**: Assigns a user to a task.

### Update Task Status
- **Path**: `PATCH /api/mission-control/v1/tasks/{id}/status/{status}`
- **Input**: 
    - `id`: Path variable (UUID)
    - `status`: Path variable (MissionStatus)
    - `TaskDto`: Optional request body (JSON) to provide `blockedReason`
- **Output**: `TaskDto` (JSON)
- **Description**: Updates the status of a task. If status is BLOCKED, the `blockedReason` can be provided in the request body.

### Get Tasks by User
- **Path**: `GET /api/mission-control/v1/tasks/user/{userId}`
- **Input**: `userId` (Path variable, UUID)
- **Output**: `List<TaskDto>` (JSON)
- **Description**: Retrieves all tasks assigned to a specific user.
