package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;

    public int getBalance(long userId) {
        return userPointRepository.findByUserId(userId)
                .map(UserPoint::getBalance)
                .orElse(0);
    }
}
