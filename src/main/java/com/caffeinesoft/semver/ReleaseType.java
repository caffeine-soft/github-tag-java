package com.caffeinesoft.semver;

public enum ReleaseType {
    MAJOR, MINOR, PATCH, PREMAJOR, PREMINOR, PREPATCH, PRERELEASE, CUSTOM, NONE;

    public static ReleaseType fromString(String type) {
        if (type == null || type.isBlank() || type.equalsIgnoreCase("false")) return NONE;
        try {
            return ReleaseType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}