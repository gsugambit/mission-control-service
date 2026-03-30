CREATE TABLE IF NOT EXISTS task_comments
(
    id            UUID PRIMARY KEY,
    task_id       UUID      NOT NULL,
    user_id       UUID      NOT NULL,
    comment       TEXT      NOT NULL,
    date_created  TIMESTAMP WITH TIME ZONE NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_task_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_task_comments_task_id ON task_comments (task_id);
