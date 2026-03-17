package com.caffeinesoft.semver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SemVer(int major, int minor, int patch, String prerelease, String build) implements Comparable<SemVer> {

    private static final Pattern SEMVER_PATTERN = Pattern.compile(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][a-zA-Z0-9-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][a-zA-Z0-9-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );

    public static SemVer parse(String version) {
        if (version == null || version.isBlank()) return null;
        Matcher matcher = SEMVER_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            return null;
        }
        return new SemVer(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(4),
                matcher.group(5)
        );
    }

    public static boolean isValid(String version) {
        return parse(version) != null;
    }

    public boolean isPrerelease() {
        return prerelease != null && !prerelease.isBlank();
    }

    public String getVersionString() {
        StringBuilder sb = new StringBuilder().append(major).append(".").append(minor).append(".").append(patch);
        if (isPrerelease()) sb.append("-").append(prerelease);
        if (build != null && !build.isBlank()) sb.append("+").append(build);
        return sb.toString();
    }

    public SemVer increment(ReleaseType type, String identifier) {
        int newMajor = major, newMinor = minor, newPatch = patch;
        String newPrerelease = null;

        switch (type) {
            case MAJOR -> { newMajor++; newMinor = 0; newPatch = 0; }
            case MINOR -> { newMinor++; newPatch = 0; }
            case PATCH -> { if (!isPrerelease()) newPatch++; }
            case PREMAJOR -> { newMajor++; newMinor = 0; newPatch = 0; newPrerelease = identifier + ".0"; }
            case PREMINOR -> { newMinor++; newPatch = 0; newPrerelease = identifier + ".0"; }
            case PREPATCH -> { newPatch++; newPrerelease = identifier + ".0"; }
            case PRERELEASE -> {
                if (isPrerelease() && prerelease.startsWith(identifier)) {
                    String[] parts = prerelease.split("\\.");
                    if (parts.length > 1 && parts[parts.length - 1].matches("\\d+")) {
                        newPrerelease = identifier + "." + (Integer.parseInt(parts[parts.length - 1]) + 1);
                    } else {
                        newPrerelease = identifier + ".0";
                    }
                } else {
                    newPrerelease = identifier + ".0";
                }
            }
            default -> throw new IllegalArgumentException("Unsupported release type: " + type);
        }
        return new SemVer(newMajor, newMinor, newPatch, newPrerelease, build);
    }

    @Override
    public int compareTo(SemVer other) {
        if (this.major != other.major) return Integer.compare(this.major, other.major);
        if (this.minor != other.minor) return Integer.compare(this.minor, other.minor);
        if (this.patch != other.patch) return Integer.compare(this.patch, other.patch);

        if (this.isPrerelease() && !other.isPrerelease()) return -1;
        if (!this.isPrerelease() && other.isPrerelease()) return 1;
        if (this.isPrerelease() && other.isPrerelease()) {
            return this.prerelease.compareTo(other.prerelease);
        }
        return 0;
    }
}
