ALTER TABLE users ADD CONSTRAINT uq_users_user_name UNIQUE (user_name);
ALTER TABLE projects ADD CONSTRAINT uq_projects_name UNIQUE (name);
