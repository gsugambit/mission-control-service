CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    acceptance_criteria TEXT,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id)
);
