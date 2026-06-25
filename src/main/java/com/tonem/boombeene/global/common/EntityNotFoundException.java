package com.tonem.boombeene.global.common;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Object identifier) {
        super(entityName + " not found: " + identifier);
    }
}
