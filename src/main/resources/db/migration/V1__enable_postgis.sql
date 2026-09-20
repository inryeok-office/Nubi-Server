CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE app_metadata (
    id BIGSERIAL PRIMARY KEY,
    metadata_key VARCHAR(100) NOT NULL UNIQUE,
    metadata_value VARCHAR(500) NOT NULL
);

INSERT INTO app_metadata (metadata_key, metadata_value)
VALUES ('schema-purpose', 'NUBI technical PoC');
