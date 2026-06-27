CREATE TABLE crowd_reports (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    store_id   BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    level      VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    -- 혼잡도 집계: "최근 30분 내 이 가게의 제보 목록"
    INDEX idx_crowd_reports_store_created (store_id, created_at),
    -- 쿨다운 검증: "이 유저가 이 가게를 최근 30분 내 제보했나?"
    INDEX idx_crowd_reports_user_store_created (user_id, store_id, created_at)
);
