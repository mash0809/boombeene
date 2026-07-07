# Boombeene 락 성능 비교

## 사전 준비

- 로컬에 JMeter 설치 (예: `brew install jmeter`)
- 로컬에서 MySQL, Redis 실행

## 포인트 락 성능 비교 순서

1. `sql/005_add_version_to_user_points.sql`, `loadtest/sql/point_bench_seed.sql` 스크립트를 순서대로 적용
2. 애플리케이션을 실행

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

3. JMeter를 실행한다:
- point-lock-benchmark.jmx 파일 내 lockType Argument 를 변경하면서 jmeter 를 실행
- "PESSIMISTIC" (비관적 락), "OPTIMISTIC" (낙관적 락), "REDIS" (Redis 락) 중 1가지 선택
```html
<elementProp name="lockType" elementType="Argument">
    <stringProp name="Argument.name">lockType</stringProp>
    <stringProp name="Argument.value">"PESSIMISTIC" | "OPTIMISTIC | "REDIS"</stringProp>
    <stringProp name="Argument.metadata">=</stringProp>
</elementProp>
```

```bash
jmeter -n -t loadtest/point-lock-benchmark.jmx -l loadtest/results/point-lock-{lockType}.jtl
```

4. 데이터가 잘 반영되었는 확인하기 위해 각 전략을 실행하기 전에 테스트용 사용자 데이터를 초기화:

```sql
UPDATE user_points SET balance = 0, version = 0 WHERE user_id = 900001;
DELETE FROM point_ledger WHERE user_id = 900001;
```

5. 결과 확인
- [결과 정리 블로그 글](https://velog.io/@mash809/%EC%82%AC%EC%9D%B4%EB%93%9C-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EB%A9%80%ED%8B%B0-%EC%9D%B8%EC%8A%A4%ED%84%B4%EC%8A%A4-%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C%EC%9D%98-lock)
