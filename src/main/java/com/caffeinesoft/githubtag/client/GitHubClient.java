package com.caffeinesoft.githubtag.client;

import com.caffeinesoft.githubtag.model.CommitReference;
import com.caffeinesoft.githubtag.model.Tag;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GitHubClient {
    private final HttpClient httpClient;
    private final String githubToken;
    private final String repository; // Format: "owner/repo"

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String API_VERSION_HEADER = "2026-03-10";

    public GitHubClient(String githubToken, String repository) {
        this.githubToken = githubToken;
        this.repository = repository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private HttpRequest.Builder baseRequestBuilder(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_URL + "/repos/" + repository + endpoint))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("X-GitHub-Api-Version", API_VERSION_HEADER);
    }

    /**
     * Fetch all tags for a given repository iteratively
     */
    public List<Tag> listTags(boolean shouldFetchAllTags) throws Exception {
        List<Tag> allTags = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            HttpRequest request = baseRequestBuilder("/tags?per_page=100&page=" + page)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch tags: " + response.body());
            }

            JsonNode tagsArray = MAPPER.readTree(response.body());
            int fetchedCount = 0;

            if (tagsArray.isArray()) {
                for (JsonNode tagNode : tagsArray) {
                    fetchedCount++;
                    JsonNode commitNode = tagNode.path("commit");

                    CommitReference commitRef = new CommitReference(
                            commitNode.path("sha") .asString(""),
                            commitNode.path("url") .asString("")
                    );

                    Tag tag = new Tag(
                            tagNode.path("name") .asString(""),
                            commitRef,
                            tagNode.path("zipball_url") .asString(""),
                            tagNode.path("tarball_url") .asString(""),
                            tagNode.path("node_id") .asString("")
                    );
                    allTags.add(tag);
                }
            }

            if (!shouldFetchAllTags || fetchedCount < 100) {
                hasMore = false;
            } else {
                page++;
            }
        }
        return allTags;
    }

    /**
     * Compare 'baseRef' to 'headRef'
     */
    public List<String> compareCommits(String baseRef, String headRef) throws Exception {
        HttpRequest request = baseRequestBuilder("/compare/" + baseRef + "..." + headRef)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to compare commits (" + baseRef + "..." + headRef + "): " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode commitsArray = root.path("commits");

        List<String> messages = new ArrayList<>();
        if (commitsArray.isArray()) {
            for (JsonNode commitNode : commitsArray) {
                String message = commitNode.path("commit").path("message") .asString(null);
                if (message != null && !message.isBlank()) {
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    /**
     * Create an annotated tag and/or push a new tag reference to the repository
     */
    public void createTag(String newTag, boolean createAnnotatedTag, String targetSha) throws Exception {
        String finalSha = targetSha;

        if (createAnnotatedTag) {
            System.out.println("Creating annotated tag object in Git...");
            String tagBody = MAPPER.createObjectNode()
                    .put("tag", newTag)
                    .put("message", newTag)
                    .put("object", targetSha)
                    .put("type", "commit")
                    .toString();

            HttpRequest createTagRequest = baseRequestBuilder("/git/tags")
                    .POST(HttpRequest.BodyPublishers.ofString(tagBody))
                    .build();

            HttpResponse<String> tagResponse = httpClient.send(createTagRequest, HttpResponse.BodyHandlers.ofString());

            if (tagResponse.statusCode() != 201) {
                throw new RuntimeException("Failed to create annotated tag object: " + tagResponse.body());
            }

            JsonNode responseNode = MAPPER.readTree(tagResponse.body());
            finalSha = responseNode.path("sha") .asString(targetSha);
        }

        System.out.println("Pushing new tag reference to the repository...");
        String refBody = MAPPER.createObjectNode()
                .put("ref", "refs/tags/" + newTag)
                .put("sha", finalSha)
                .toString();

        HttpRequest createRefRequest = baseRequestBuilder("/git/refs")
                .POST(HttpRequest.BodyPublishers.ofString(refBody))
                .build();

        HttpResponse<String> refResponse = httpClient.send(createRefRequest, HttpResponse.BodyHandlers.ofString());

        if (refResponse.statusCode() != 201) {
            throw new RuntimeException("Failed to create tag reference: " + refResponse.body());
        }
    }
}