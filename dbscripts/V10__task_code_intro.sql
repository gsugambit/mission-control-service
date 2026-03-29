ALTER TABLE projects
  ALTER COLUMN prefix SET NOT NULL;

-- 1. Create the Base32 Encoder Function
CREATE OR REPLACE FUNCTION crockford_base32_encode(num BIGINT)
RETURNS TEXT AS $$
DECLARE
alphabet TEXT := '0123456789ABCDEFGHJKMNPQRSTVWXYZ';
    result TEXT := '';
    remainder INT;
BEGIN
    IF num = 0 THEN RETURN '0'; END IF;
    WHILE num > 0 LOOP
        remainder := num % 32;
        result := substr(alphabet, remainder + 1, 1) || result;
        num := num / 32;
END LOOP;
RETURN result;
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;

-- 2. Add the task_code column (Nullable initially, and UNIQUE)
ALTER TABLE tasks
  ADD COLUMN task_code TEXT UNIQUE;

-- 3. Create and attach the Trigger
CREATE OR REPLACE FUNCTION assign_project_task_code()
RETURNS TRIGGER AS $$
DECLARE
proj_prefix TEXT;
    seq_num BIGINT;
BEGIN
UPDATE projects
SET next_task_seq = next_task_seq + 1
WHERE id = NEW.project_id
  RETURNING prefix, next_task_seq - 1 INTO proj_prefix, seq_num;

NEW.task_code := proj_prefix || '-' || crockford_base32_encode(seq_num);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_task_code
  BEFORE INSERT ON tasks
  FOR EACH ROW
  EXECUTE FUNCTION assign_project_task_code();

-- 4. Backfill all EXISTING tasks
DO $$
DECLARE
task_row RECORD;
    proj_prefix TEXT;
    seq_num BIGINT;
BEGIN
FOR task_row IN SELECT id, project_id FROM tasks WHERE task_code IS NULL LOOP
-- Increment the sequence for the project safely
UPDATE projects
SET next_task_seq = next_task_seq + 1
WHERE id = task_row.project_id
  RETURNING prefix, next_task_seq - 1 INTO proj_prefix, seq_num;

-- Assign the code to the old task
UPDATE tasks
SET task_code = proj_prefix || '-' || crockford_base32_encode(seq_num)
WHERE id = task_row.id;
END LOOP;
END $$;

-- 5. Lock it down! Now that every task has a code, make it NOT NULL
ALTER TABLE tasks
  ALTER COLUMN task_code SET NOT NULL;