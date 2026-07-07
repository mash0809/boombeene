-- 낙관적 락 테스트를 위한 version 컬럼 추가
ALTER TABLE user_points
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER balance;
