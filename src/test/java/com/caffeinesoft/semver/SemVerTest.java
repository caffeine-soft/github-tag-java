package com.caffeinesoft.semver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemVerTest {

    @Test
    void testValidParse() {
        SemVer version = SemVer.parse("1.2.3-alpha.1+build.123");
        assertNotNull(version);
        assertEquals(1, version.major());
        assertEquals(2, version.minor());
        assertEquals(3, version.patch());
        assertEquals("alpha.1", version.prerelease());
        assertEquals("build.123", version.build());
    }

    @Test
    void testInvalidParse() {
        assertNull(SemVer.parse("invalid-version"));
        assertNull(SemVer.parse("1.2"));
    }

    @Test
    void testIncrementMajor() {
        SemVer version = SemVer.parse("1.2.3");
        SemVer bumped = version.increment(ReleaseType.MAJOR, "");
        assertEquals("2.0.0", bumped.getVersionString());
    }

    @Test
    void testIncrementMinor() {
        SemVer version = SemVer.parse("1.2.3");
        SemVer bumped = version.increment(ReleaseType.MINOR, "");
        assertEquals("1.3.0", bumped.getVersionString());
    }

    @Test
    void testIncrementPatch() {
        SemVer version = SemVer.parse("1.2.3");
        SemVer bumped = version.increment(ReleaseType.PATCH, "");
        assertEquals("1.2.4", bumped.getVersionString());
    }
}