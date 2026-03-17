package com.caffeinesoft.github.client;

import com.caffeinesoft.github.tag.model.CommitReference;
import com.caffeinesoft.github.tag.model.Tag;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private final String repository;

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

            JSONArray tagsArray = new JSONArray(response.body());
            int fetchedCount = tagsArray.length();

            for (int i = 0; i < fetchedCount; i++) {
                JSONObject tagNode = tagsArray.getJSONObject(i);
                JSONObject commitNode = tagNode.optJSONObject("commit");

                if (commitNode == null) commitNode = new JSONObject();

                CommitReference commitRef = new CommitReference(
                        commitNode.optString("sha", ""),
                        commitNode.optString("url", "")
                );

                Tag tag = new Tag(
                        tagNode.optString("name", ""),
                        commitRef,
                        tagNode.optString("zipball_url", ""),
                        tagNode.optString("tarball_url", ""),
                        tagNode.optString("node_id", "")
                );
                allTags.add(tag);
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

        JSONObject root = new JSONObject(response.body());
        JSONArray commitsArray = root.optJSONArray("commits");

        List<String> messages = new ArrayList<>();
        if (commitsArray != null) {
            for (int i = 0; i < commitsArray.length(); i++) {
                JSONObject commitItem = commitsArray.getJSONObject(i);
                JSONObject commitNode = commitItem.optJSONObject("commit");

                if (commitNode != null) {
                    String message = commitNode.optString("message", null);
                    if (message != null && !message.isBlank()) {
                        messages.add(message);
                    }
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
            String tagBody = new JSONObject()
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

            JSONObject responseNode = new JSONObject(tagResponse.body());
            finalSha = responseNode.optString("sha", targetSha);
        }

        System.out.println("Pushing new tag reference to the repository...");
        String refBody = new JSONObject()
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