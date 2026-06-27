CREATE TABLE user_points (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    balance    INT         NOT NULL DEFAULT 0,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_points_user_id (user_id)
);

CREATE TABLE point_ledger (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    type            VARCHAR(10)  NOT NULL,
    amount          INT          NOT NULL,
    report_id       BIGINT,
    description     VARCHAR(255),
    idempotency_key VARCHAR(100) NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_point_ledger_idempotency (idempotency_key),
    INDEX idx_point_ledger_user_id (user_id)
);
