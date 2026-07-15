package com.tonem.boombeene;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    @Test
    @DisplayName("모듈 간 경계와 의존성 규칙을 검증한다")
    void verifiesModuleBoundaries() {
        var modules = ApplicationModules.of(BoombeeneApplication.class);

        // 모듈 정보 출력
        modules.forEach(System.out::println);

        // 모듈 의존성 verify
        modules.verify();
    }
}
