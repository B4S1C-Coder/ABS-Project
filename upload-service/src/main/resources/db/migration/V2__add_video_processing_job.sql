CREATE TABLE video_processing_jobs (
  id UUID PRIMARY KEY,
  video_id UUID NOT NULL,
  status VARCHAR(50) NOT NULL,
  worker_id VARCHAR(128),
  lease_until TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_video_processing_jobs_video
    FOREIGN KEY (video_id)
    REFERENCES videos(id)
    ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_video_processing_jobs_video
  ON video_processing_jobs(video_id);

CREATE INDEX idx_video_processing_jobs_lease
  ON video_processing_jobs(lease_until);