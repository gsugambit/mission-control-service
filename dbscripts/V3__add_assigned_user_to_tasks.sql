ALTER TABLE tasks ADD COLUMN assigned_user_id UUID;
ALTER TABLE tasks ADD CONSTRAINT fk_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id);
