package com.omnify.auth.notification;

public class DuplicateRegistrationEvent {

    private final String email;

    public DuplicateRegistrationEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}