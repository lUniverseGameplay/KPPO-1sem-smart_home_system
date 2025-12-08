package com.example.smart_home_syst.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException (String exc) {
        super(exc);
    }
}
