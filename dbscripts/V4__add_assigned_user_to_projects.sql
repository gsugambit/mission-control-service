ALTER TABLE projects ADD COLUMN assigned_user_id UUID;
ALTER TABLE projects ADD CONSTRAINT fk_project_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id);
