package com.caffeinesoft.semver;

import com.caffeinesoft.github.tag.model.CommitReference;
import com.caffeinesoft.github.tag.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TagAnalyzerTest {

    private final Pattern prefixRegex = Pattern.compile("^v");

    private Tag createMockTag(String name) {
        return new Tag(name, new CommitReference("dummySha", ""), "", "", "");
    }

    @Test
    void testGetValidTags_FiltersAndSortsDescending() {
        List<Tag> rawTags = List.of(
                createMockTag("v1.0.0"),
                createMockTag("v2.0.0"),
                createMockTag("1.5.0"),
                createMockTag("v1.5.0-beta.1"),
                createMockTag("random-tag")
        );

        List<Tag> validTags = TagAnalyzer.getValidTags(rawTags, prefixRegex);

        assertEquals(3, validTags.size());
        assertEquals("v2.0.0", validTags.get(0).name());
        assertEquals("v1.5.0-beta.1", validTags.get(1).name());
        assertEquals("v1.0.0", validTags.get(2).name());
    }

    @Test
    void testGetLatestTag_ReturnsHighestStable() {
        List<Tag> validTags = List.of(
                createMockTag("v2.0.0"),
                createMockTag("v2.1.0-alpha"),
                createMockTag("v1.5.0")
        );

        Tag latest = TagAnalyzer.getLatestTag(validTags, prefixRegex, "v");
        assertNotNull(latest);
        assertEquals("v2.0.0", latest.name());
    }

    @Test
    void testGetLatestTag_EmptyListReturnsDefault() {
        List<Tag> validTags = List.of();

        Tag latest = TagAnalyzer.getLatestTag(validTags, prefixRegex, "v");
        assertNotNull(latest);
        assertEquals("v0.0.0", latest.name());
    }

    @Test
    void testGetLatestPrereleaseTag_MatchesIdentifier() {
        List<Tag> validTags = List.of(
                createMockTag("v2.0.0-beta.2"),
                createMockTag("v2.0.0-beta.1"),
                createMockTag("v2.0.0-alpha.1")
        );

        Tag latestBeta = TagAnalyzer.getLatestPrereleaseTag(validTags, "beta", prefixRegex);
        assertNotNull(latestBeta);
        assertEquals("v2.0.0-beta.2", latestBeta.name());

        Tag latestAlpha = TagAnalyzer.getLatestPrereleaseTag(validTags, "alpha", prefixRegex);
        assertNotNull(latestAlpha);
        assertEquals("v2.0.0-alpha.1", latestAlpha.name());

        Tag latestRc = TagAnalyzer.getLatestPrereleaseTag(validTags, "rc", prefixRegex);
        assertNull(latestRc);
    }
}