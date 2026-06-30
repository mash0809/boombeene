package com.tonem.boombeene.point;

import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointApi {

    private final UserPointRepository userPointRepository;

    public PointInfo getByUserId(long userId) {
        UserPoint userPoint = userPointRepository.findByUserId(userId)
                .orElse(null);

        return new PointInfo(userPoint == null ? 0 : userPoint.getBalance());
    }
}
