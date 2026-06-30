package com.tonem.boombeene.common.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Object identifier) {
        super(entityName + " not found: " + identifier);
    }
}
