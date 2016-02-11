/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.vfs.impl.fs;

import java.net.URI;

/** @author andrew00x */
public class GitUrlResolverTest extends LocalFileSystemTest {
    private String file;
    private final String GIT_SERVER_URI_PREFIX = "git";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final String testFolderPath = createDirectory(testRootPath, "GitUrlResolveTest_Folder");
        file = createFile(testFolderPath, "test.txt", DEFAULT_CONTENT_BYTES);
    }

    public void testResolveGitUrlWithPort() throws Exception {
        String path = root.toPath().relativize(getIoFile(file).toPath()).toString();
        path = path.replaceAll("[\\\\]", "/");
        String expectedUrl = String.format("http://localhost:9000/%s/%s", GIT_SERVER_URI_PREFIX, path);

        GitUrlResolver resolver = new GitUrlResolver(root, GIT_SERVER_URI_PREFIX, new LocalPathResolver());
        final String url = resolver.resolve(URI.create("http://localhost:9000/some/path"), mountPoint.getVirtualFile(file));
        assertEquals(expectedUrl, url);
    }

    public void testResolveGitUrlWithPort2() throws Exception {
        String folder = file.substring(0, file.lastIndexOf("/"));
        System.out.println(folder);
        String path = root.toPath().relativize(getIoFile(folder).toPath()).toString();
        path = path.replaceAll("[\\\\]", "/");
        String expectedUrl = String.format("http://localhost:9000/%s/%s", GIT_SERVER_URI_PREFIX, path);

        GitUrlResolver resolver = new GitUrlResolver(root, GIT_SERVER_URI_PREFIX, new LocalPathResolver());
        final String url = resolver.resolve(URI.create("http://localhost:9000/some/path"), mountPoint.getVirtualFile(folder));
        assertEquals(expectedUrl, url);
    }

    public void testResolveGitUrlWithoutPort() throws Exception {
        String path = root.toPath().relativize(getIoFile(file).toPath()).toString();
        path = path.replaceAll("[\\\\]", "/");
        String expectedUrl = String.format("http://localhost/%s/%s", GIT_SERVER_URI_PREFIX, path);

        GitUrlResolver resolver = new GitUrlResolver(root, GIT_SERVER_URI_PREFIX, new LocalPathResolver());
        final String url = resolver.resolve(URI.create("http://localhost/some/path"), mountPoint.getVirtualFile(file));
        assertEquals(expectedUrl, url);
    }

    public void testResolveGitUrlWithoutPort2() throws Exception {
        String folder = file.substring(0, file.lastIndexOf("/"));
        String path = root.toPath().relativize(getIoFile(folder).toPath()).toString();
        path = path.replaceAll("[\\\\]", "/");
        String expectedUrl = String.format("http://localhost/%s/%s", GIT_SERVER_URI_PREFIX, path);

        GitUrlResolver resolver = new GitUrlResolver(root, GIT_SERVER_URI_PREFIX, new LocalPathResolver());
        final String url = resolver.resolve(URI.create("http://localhost/some/path"), mountPoint.getVirtualFile(folder));
        assertEquals(expectedUrl, url);
    }

    public void testResolveGitUrlWithHttps() throws Exception {
        String path = root.toPath().relativize(getIoFile(file).toPath()).toString();
        path = path.replaceAll("[\\\\]", "/");
        String expectedUrl = String.format("https://localhost:9000/%s/%s", GIT_SERVER_URI_PREFIX, path);

        GitUrlResolver resolver = new GitUrlResolver(root, GIT_SERVER_URI_PREFIX, new LocalPathResolver());
        final String url = resolver.resolve(URI.create("https://localhost:9000/some/path"), mountPoint.getVirtualFile(file));
        assertEquals(expectedUrl, url);
    }
}
