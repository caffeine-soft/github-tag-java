package com.caffeinesoft.githubtag.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class ActionCore {

    public static String getInput(String name) {
        String envVar = "INPUT_" + name.toUpperCase();
        return Optional.ofNullable(System.getenv(envVar)).orElse("");
    }

    public static void setOutput(String name, String value) {
        String githubOutput = System.getenv("GITHUB_OUTPUT");
        if (githubOutput != null && !githubOutput.isBlank()) {
            try {
                String outputLine = name + "=" + value + System.lineSeparator();
                Files.writeString(Path.of(githubOutput), outputLine, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Failed to write to GITHUB_OUTPUT: " + e.getMessage());
            }
        } else {
            System.out.println("::set-output name=" + name + "::" + value);
        }
    }

    public static void info(String message) {
        System.out.println(message);
    }

    public static void setFailed(String message) {
        System.err.println("::error::" + message);
        System.exit(1);
    }
}