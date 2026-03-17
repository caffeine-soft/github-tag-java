package com.caffeinesoft.githubtag.core;

import com.caffeinesoft.semver.ReleaseType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommitAnalyzerTest {

    private final CommitAnalyzer analyzer = new CommitAnalyzer();

    @Test
    void testMinorBumpFeatures() {
        assertEquals(ReleaseType.MINOR, analyzer.analyzeCommit("feat: add login page"));
        assertEquals(ReleaseType.MINOR, analyzer.analyzeCommit("feature(auth): token refresh"));
    }

    @Test
    void testPatchBumpFixes() {
        assertEquals(ReleaseType.PATCH, analyzer.analyzeCommit("fix: resolve null pointer"));
        assertEquals(ReleaseType.PATCH, analyzer.analyzeCommit("bugfix(ui): align button"));
        assertEquals(ReleaseType.PATCH, analyzer.analyzeCommit("perf: optimize query"));
    }

    @Test
    void testMajorBumpBreakingChanges() {
        assertEquals(ReleaseType.MAJOR, analyzer.analyzeCommit("breaking: remove old api"));
        assertEquals(ReleaseType.MAJOR, analyzer.analyzeCommit("break(core): refactor engine"));
    }

    @Test
    void testNoBumpForChoresAndDocs() {
        assertEquals(ReleaseType.NONE, analyzer.analyzeCommit("docs: update readme"));
        assertEquals(ReleaseType.NONE, analyzer.analyzeCommit("chore: clean up warnings"));
        assertEquals(ReleaseType.NONE, analyzer.analyzeCommit("Merge pull request #123"));
    }
}