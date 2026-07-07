-- 부하 테스트용 시드 데이터 삽입

-- 부하 테스트용 유저 생성
INSERT INTO users (id, email, password, nickname)
VALUES (900001, 'point-bench@example.com', '{noop}password', 'point-bench')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 해당 유저의 user_point 미리 생성
INSERT INTO user_points (user_id, balance, version, updated_at)
VALUES (900001, 0, 0, NOW(6))
ON DUPLICATE KEY UPDATE balance = 0, version = 0, updated_at = NOW(6);

-- 부하 테스트용 store 데이터 생성 (100 개)
INSERT INTO stores (place_id, name, latitude, longitude, category)
SELECT CONCAT('bench-store-', seq.n), CONCAT('Bench Store ', seq.n),
       37.5662952 + (seq.n * 0.00001), 126.9779451 + (seq.n * 0.00001), 'CAFE'
FROM (
    SELECT a.N + b.N * 10 + 1 AS n
    FROM (
        SELECT 0 N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) a
    CROSS JOIN (
        SELECT 0 N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) b
) seq
ON DUPLICATE KEY UPDATE name = VALUES(name), latitude = VALUES(latitude), longitude = VALUES(longitude);
