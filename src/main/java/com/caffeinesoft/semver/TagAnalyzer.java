package com.caffeinesoft.semver;

import com.caffeinesoft.githubtag.model.CommitReference;
import com.caffeinesoft.githubtag.model.Tag;

import java.util.List;
import java.util.regex.Pattern;

public class TagAnalyzer {

    public static List<Tag> getValidTags(List<Tag> tags, Pattern prefixRegex) {
        return tags.stream()
                .filter(tag -> prefixRegex.matcher(tag.name()).find())
                .filter(tag -> SemVer.isValid(prefixRegex.matcher(tag.name()).replaceFirst("")))
                .sorted((a, b) -> {
                    SemVer v1 = SemVer.parse(prefixRegex.matcher(a.name()).replaceFirst(""));
                    SemVer v2 = SemVer.parse(prefixRegex.matcher(b.name()).replaceFirst(""));
                    return v2.compareTo(v1); // Reverse sort (rcompare)
                })
                .toList();
    }

    public static Tag getLatestTag(List<Tag> validTags, Pattern prefixRegex, String tagPrefix) {
        return validTags.stream()
                .filter(tag -> {
                    SemVer v = SemVer.parse(prefixRegex.matcher(tag.name()).replaceFirst(""));
                    return v != null && !v.isPrerelease();
                })
                .findFirst()
                .orElse(new Tag(tagPrefix + "0.0.0", new CommitReference("HEAD", ""), "", "", ""));
    }

    public static Tag getLatestPrereleaseTag(List<Tag> validTags, String identifier, Pattern prefixRegex) {
        return validTags.stream()
                .filter(tag -> {
                    SemVer v = SemVer.parse(prefixRegex.matcher(tag.name()).replaceFirst(""));
                    return v != null && v.isPrerelease() && v.prerelease().contains(identifier);
                })
                .findFirst()
                .orElse(null);
    }
}
