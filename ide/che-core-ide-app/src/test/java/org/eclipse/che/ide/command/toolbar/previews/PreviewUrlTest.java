package org.eclipse.che.ide.command.toolbar.previews;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests for {@link PreviewUrl}. */
public class PreviewUrlTest {

    private static final String URL          = "http://preview.com";
    private static final String DISPLAY_NAME = "dev-machine:8080";

    private PreviewUrl previewUrl;

    @Before
    public void setUp() {
        previewUrl = new PreviewUrl(URL, DISPLAY_NAME);
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals(URL, previewUrl.getUrl());
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals(DISPLAY_NAME, previewUrl.getDisplayName());
    }
}
