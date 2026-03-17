package com.caffeinesoft.github.tag.core;

import com.caffeinesoft.semver.ReleaseType;

import java.util.regex.Pattern;

public class CommitAnalyzer {

    public ReleaseType analyzeCommit(String commitMessage) {
        if (commitMessage == null || commitMessage.isBlank()) {
            return ReleaseType.NONE;
        }

        String type = extractType(commitMessage);

        return switch (type.toLowerCase()) {
            case "break", "breaking" -> ReleaseType.MAJOR;
            case "feat", "feature" -> ReleaseType.MINOR;
            case "fix", "bugfix", "perf" -> ReleaseType.PATCH;
            default -> ReleaseType.NONE;
        };
    }

    private String extractType(String message) {
        var matcher = Pattern.compile("^([a-zA-Z]+)(\\(.*\\))?:.*").matcher(message);
        return matcher.matches() ? matcher.group(1) : "unknown";
    }
}