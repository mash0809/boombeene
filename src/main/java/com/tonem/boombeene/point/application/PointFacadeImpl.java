package com.tonem.boombeene.point.application;

import com.tonem.boombeene.point.api.PointFacade;
import com.tonem.boombeene.point.api.PointInfo;
import com.tonem.boombeene.point.entity.UserPoint;
import com.tonem.boombeene.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointFacadeImpl implements PointFacade {

    private final UserPointRepository userPointRepository;

    @Override
    public PointInfo getByUserId(long userId) {
        UserPoint userPoint = userPointRepository.findByUserId(userId)
                .orElse(null);

        return new PointInfo(userPoint == null ? 0 : userPoint.getBalance());
    }
}
