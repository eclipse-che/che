package org.eclipse.che.ide.ext.java.client;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link JavaUtils}
 */
public class JavaUtilsTest {
    @Test
    public void shouldValidatePackageNameWithCorrectContent() throws Exception {
        assertTrue(JavaUtils.isValidPackageName("Package_name"));
    }

    @Test
    public void shouldInvalidatePackageNameWithWhiteSpaces() throws Exception {
        assertFalse(JavaUtils.isValidPackageName("Package name"));
    }

    @Test
    public void shouldInvalidatePackageNameWithMinuses() throws Exception {
        assertFalse(JavaUtils.isValidPackageName("Package-name"));
    }

    @Test
    public void shouldInvalidatePackageNameStartingWithNumbers() throws Exception {
        assertFalse(JavaUtils.isValidPackageName("1Package_name"));
    }

    @Test
    public void shouldInvalidatePackageNameStartingWithSpaces() throws Exception {
        assertFalse(JavaUtils.isValidPackageName(" Package_name"));
    }
}