package com.caffeinesoft.githubtag;

import com.caffeinesoft.githubtag.client.GitHubClient;
import com.caffeinesoft.githubtag.core.CommitAnalyzer;
import com.caffeinesoft.githubtag.model.Tag;
import com.caffeinesoft.githubtag.utils.ActionCore;
import com.caffeinesoft.semver.ReleaseType;
import com.caffeinesoft.semver.SemVer;
import com.caffeinesoft.semver.TagAnalyzer;

import java.util.List;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        try {
            runAction();
        } catch (Exception e) {
            ActionCore.setFailed("Action failed: " + e.getMessage());
        }
    }

    private static void runAction() throws Exception {
        String defaultBumpInput = ActionCore.getInput("default_bump");
        String defaultPreReleaseBumpInput = ActionCore.getInput("default_prerelease_bump");
        String tagPrefix = ActionCore.getInput("tag_prefix");
        String appendToPreReleaseTag = ActionCore.getInput("append_to_pre_release_tag");
        boolean dryRun = Boolean.parseBoolean(ActionCore.getInput("dry_run"));
        boolean fetchAllTags = Boolean.parseBoolean(ActionCore.getInput("fetch_all_tags"));
        boolean createAnnotatedTag = Boolean.parseBoolean(ActionCore.getInput("create_annotated_tag"));

        String githubSha = System.getenv("GITHUB_SHA");
        String commitShaInput = ActionCore.getInput("commit_sha");
        String commitRef = (commitShaInput != null && !commitShaInput.isBlank()) ? commitShaInput : githubSha;

        String githubRef = System.getenv("GITHUB_REF");
        String githubRepository = System.getenv("GITHUB_REPOSITORY");
        String githubToken = ActionCore.getInput("github_token");

        String currentBranch = githubRef.replace("refs/heads/", "");
        String identifier = (appendToPreReleaseTag != null && !appendToPreReleaseTag.isBlank()
                ? appendToPreReleaseTag
                : currentBranch).replaceAll("[^a-zA-Z0-9-]", "-");

        Pattern prefixRegex = Pattern.compile("^" + tagPrefix);

        GitHubClient client = new GitHubClient(githubToken, githubRepository);
        List<Tag> allTags = client.listTags(fetchAllTags);
        List<Tag> validTags = TagAnalyzer.getValidTags(allTags, prefixRegex);

        Tag latestTag = TagAnalyzer.getLatestTag(validTags, prefixRegex, tagPrefix);
        Tag latestPrereleaseTag = TagAnalyzer.getLatestPrereleaseTag(validTags, identifier, prefixRegex);

        Tag previousTag;
        if (latestPrereleaseTag == null) {
            previousTag = latestTag;
        } else {
            SemVer latestVer = SemVer.parse(prefixRegex.matcher(latestTag.name()).replaceFirst(""));
            SemVer preVer = SemVer.parse(prefixRegex.matcher(latestPrereleaseTag.name()).replaceFirst(""));
            previousTag = (latestVer.compareTo(preVer) >= 0) ? latestTag : latestPrereleaseTag;
        }

        SemVer previousVersion = SemVer.parse(prefixRegex.matcher(previousTag.name()).replaceFirst(""));

        ActionCore.info("Previous tag was " + previousTag.name() + ", previous version was " + previousVersion.getVersionString() + ".");
        ActionCore.setOutput("previous_version", previousVersion.getVersionString());
        ActionCore.setOutput("previous_tag", previousTag.name());

        List<String> commitMessages = client.compareCommits(previousTag.commit().sha(), commitRef);
        CommitAnalyzer analyzer = new CommitAnalyzer();
        ReleaseType calculatedBump = ReleaseType.NONE;

        for (String msg : commitMessages) {
            ReleaseType type = analyzer.analyzeCommit(msg);
            if (type.compareTo(calculatedBump) < 0 && type != ReleaseType.NONE) {
                calculatedBump = type;
            }
        }

        if (calculatedBump == ReleaseType.NONE) {
            calculatedBump = ReleaseType.fromString(defaultBumpInput);
        }

        if (calculatedBump == ReleaseType.NONE) {
            ActionCore.info("No commit specifies the version bump. Skipping the tag creation.");
            return;
        }

        SemVer newVersion = previousVersion.increment(calculatedBump, identifier);
        String newTag = tagPrefix + newVersion.getVersionString();

        ActionCore.info("New version is " + newVersion.getVersionString() + ".");
        ActionCore.setOutput("new_version", newVersion.getVersionString());
        ActionCore.setOutput("new_tag", newTag);

        if (dryRun) {
            ActionCore.info("Dry run: not performing tag action.");
            return;
        }

        client.createTag(newTag, createAnnotatedTag, commitRef);    }
}