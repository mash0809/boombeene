CREATE TABLE stores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    place_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    category VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stores_place_id (place_id)
);
