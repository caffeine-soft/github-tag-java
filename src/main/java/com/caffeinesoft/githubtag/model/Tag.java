package com.caffeinesoft.githubtag.model;

public record Tag(
        String name,
        CommitReference commit,
        String zipballUrl,
        String tarballUrl,
        String nodeId
) {}

