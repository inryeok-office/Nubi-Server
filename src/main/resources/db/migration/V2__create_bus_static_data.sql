CREATE TABLE bus_route (
    route_id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    upward_destination VARCHAR(200),
    downward_destination VARCHAR(200),
    type_code VARCHAR(50),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bus_stop (
    stop_id BIGINT PRIMARY KEY,
    ars_id VARCHAR(50),
    name VARCHAR(200) NOT NULL,
    location geometry(Point, 4326) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bus_stop_location_longitude CHECK (ST_X(location) BETWEEN -180 AND 180),
    CONSTRAINT bus_stop_location_latitude CHECK (ST_Y(location) BETWEEN -90 AND 90)
);

CREATE INDEX idx_bus_stop_location_gist ON bus_stop USING GIST (location);

CREATE TABLE route_stop (
    route_id BIGINT NOT NULL REFERENCES bus_route (route_id) ON DELETE CASCADE,
    stop_id BIGINT NOT NULL REFERENCES bus_stop (stop_id) ON DELETE CASCADE,
    stop_sequence INTEGER NOT NULL CHECK (stop_sequence > 0),
    PRIMARY KEY (route_id, stop_id)
);

CREATE INDEX idx_route_stop_route_sequence ON route_stop (route_id, stop_sequence);
