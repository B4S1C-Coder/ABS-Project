CREATE TABLE videos (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    original_filename VARCHAR(255),
    s3_key VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);