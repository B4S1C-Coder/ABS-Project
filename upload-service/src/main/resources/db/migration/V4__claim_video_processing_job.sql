CREATE OR REPLACE FUNCTION claim_video_processing_job(
  p_job_id UUID, p_worker_id TEXT, p_lease_until TIMESTAMP
) RETURNS BOOLEAN AS $$
BEGIN
  UPDATE video_processing_jobs
  SET
    status = 'PROCESSING',
    worker_id = p_worker_id,
    lease_until = p_lease_until,
    updated_at = now()
  WHERE id = p_job_id
    AND (
      status = 'PENDING'
      OR (status = 'PROCESSING' AND lease_until < now())
    );
  RETURN FOUND;
END;
$$ LANGUAGE plpgsql;