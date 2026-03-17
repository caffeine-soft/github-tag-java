package com.caffeinesoft.github.tag.model;

public record Tag(
        String name,
        CommitReference commit,
        String zipballUrl,
        String tarballUrl,
        String nodeId
) {}

