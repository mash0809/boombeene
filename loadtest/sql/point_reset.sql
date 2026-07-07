-- 부하 테스트 후 데이터 원복
UPDATE user_points SET balance = 0, version = 0, updated_at = NOW(6) WHERE user_id = 900001;
DELETE FROM point_ledger WHERE user_id = 900001;
