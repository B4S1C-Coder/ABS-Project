-- Ensure created_at is always set (even outside Hibernate)
ALTER TABLE video_processing_jobs
ALTER COLUMN created_at SET DEFAULT now();

-- Function to auto-update updated_at on every UPDATE
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Idempotent trigger creation
DROP TRIGGER IF EXISTS trg_video_processing_jobs_updated
ON video_processing_jobs;

CREATE TRIGGER trg_video_processing_jobs_updated
BEFORE UPDATE ON video_processing_jobs
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
