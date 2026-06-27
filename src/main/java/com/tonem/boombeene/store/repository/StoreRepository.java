package com.tonem.boombeene.store.repository;

import com.tonem.boombeene.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByPlaceIdIn(List<String> placeIds);
}
