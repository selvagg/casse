package com.audio.casse.service;

public enum SpringProfile {
    LOCAL("local"),
    DEV("dev"),
    PROD("prod");

    private final String name;

    SpringProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
