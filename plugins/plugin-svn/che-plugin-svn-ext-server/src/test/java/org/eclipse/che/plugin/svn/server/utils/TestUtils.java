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
package org.eclipse.che.plugin.svn.server.utils;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.svn.server.SubversionApi;
import org.eclipse.che.plugin.svn.server.SubversionException;
import org.eclipse.che.plugin.svn.server.repository.RepositoryUrlProvider;
import org.eclipse.che.plugin.svn.server.upstream.CommandLineResult;
import org.eclipse.che.plugin.svn.server.upstream.UpstreamUtils;
import org.eclipse.che.plugin.svn.shared.AddRequest;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.CheckoutRequest;
import org.eclipse.che.plugin.svn.shared.CommitRequest;
import org.eclipse.che.plugin.svn.shared.Depth;
import org.eclipse.che.plugin.svn.shared.PropertySetRequest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test utilities.
 */
public class TestUtils {

    private static DtoFactory dtoFactory = DtoFactory.getInstance();

    private static SubversionApi subversionApi = new SubversionApi(new RepositoryUrlProvider() {
        @Override
        public String getRepositoryUrl(final String projectPath){
            return "";
        }
    }, null);

    public static final String[] GREEK_TREE = new String[] {
            "/",
            "/branches/",
            "/branches/2.0/",
            "/branches/2.0/A/",
            "/branches/2.0/A/mu",
            "/tags/",
            "/tags/1.0/A/",
            "/tags/1.0/A/gu",
            "/trunk/",
            "/trunk/A/",
            "/trunk/A/mu",
            "/trunk/A/B/",
            "/trunk/A/B/lambda",
            "/trunk/A/B/E/",
            "/trunk/A/B/E/alpha",
            "/trunk/A/B/E/beta",
            "/trunk/A/B/F/",
            "/trunk/A/C/",
            "/trunk/A/D/",
            "/trunk/A/D/gamma",
            "/trunk/A/D/H/",
            "/trunk/A/D/H/chi",
            "/trunk/A/D/H/psi",
            "/trunk/A/D/H/omega",
            "/trunk/A/D/G/",
            "/trunk/A/D/G/pi",
            "/trunk/A/D/G/rho",
            "/trunk/A/D/G/tau",
            "/iota"
    };

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Creates a virtual file system.
     *
     * @return the virtual file system
     * @throws Exception
     *         if anything goes wrong
     */
    public static VirtualFileSystem createVirtualFileSystem() throws Exception {
        File rootDirectory = java.nio.file.Files.createTempDirectory(null).toFile();
        VirtualFileSystemProvider vfsProvider = new LocalVirtualFileSystemProvider(rootDirectory, null);
        return vfsProvider.getVirtualFileSystem();
    }

    public static class SystemOutLineConsumer implements LineConsumer {
        @Override
        public void writeLine(String line) throws IOException {
            LOG.debug(line);
        }

        @Override
        public void close() throws IOException {
        }
    }

    public static class SystemOutLineConsumerFactory implements LineConsumerFactory {
        @Override
        public LineConsumer newLineConsumer() {
            return new SystemOutLineConsumer();
        }
    }

    /**
     * Throws an exception if the CLI result is no a valid "success" response.
     * <p/>
     * <b>Note:</b> Might make sense to move this to a core API instead of a test API
     *
     * @param result
     *         the CLI result
     * @throws Exception
     *         if the CLI result indicates an error
     */
    public static void handleCLIResult(final CommandLineResult result) throws Exception {
        if (result.getExitCode() != 0) {
            throw new SubversionException(Joiner.on("\n")
                                                .join(result.getStderr().size() != 0 ?
                                                      result.getStderr()
                                                            .toArray(new String[result.getStderr().size()]) :
                                                      result.getStdout()
                                                            .toArray(new String[result.getStdout().size()])));
        }
    }

    /**
     * Creates a user and makes that user be the current user.
     *
     * @param userProfileDao
     *         the User Profile DAO
     * @throws Exception
     *         if anything goes wrong
     */
    public static void createTestUser(final ProfileDao userProfileDao) throws Exception {
        // set current user
        EnvironmentContext.getCurrent().setSubject(new SubjectImpl("codenvy", "codenvy", null, false));

        // rules for mock
        final Map<String, String> profileAttributes = new HashMap<>();

        profileAttributes.put("firstName", "Codenvy");
        profileAttributes.put("lastName", "Codenvy");
        profileAttributes.put("email", "che@eclipse.org");

        Mockito.when(userProfileDao.getById("codenvy"))
               .thenReturn(new ProfileImpl("codenvy", profileAttributes));
    }

    /**
     * Creates a new Subversion repository in a temporary location with the Greek Tree structure within it.
     *
     * @return the path to the repository root
     * @throws Exception
     *         if anything goes wrong
     */
    public static File createGreekTreeRepository() throws Exception {
        final File repoRoot = Files.createTempDir();
        final File wcRoot = Files.createTempDir();

        // Clean up after execution
        repoRoot.deleteOnExit();
        wcRoot.deleteOnExit();

        // Create the repository
        final CommandLineResult result = UpstreamUtils.executeCommandLine(null, "svnadmin", new String[] {
                "create",
                repoRoot.getAbsolutePath()
        }, -1, repoRoot);

        // Make sure the result is fine
        handleCLIResult(result);

        // Checkout the repository
        final CLIOutputWithRevisionResponse coResponse =
                subversionApi.checkout(dtoFactory.createDto(CheckoutRequest.class)
                                                 .withProjectPath(wcRoot.getAbsolutePath())
                                                 .withUrl("file:///" + repoRoot.getAbsolutePath()));

        assertTrue(coResponse.getRevision() > -1);

        final List<String> pathsToAdd = new ArrayList<>();

        // Create the directory structure
        for (final String path : GREEK_TREE) {
            final File fileForPath = new File(wcRoot, path);
            final String[] pathParts = path.split("/");

            // Create parent directories (Probably not necessary)
            Files.createParentDirs(fileForPath);

            if (!path.endsWith("/") && fileForPath.createNewFile()) {
                Files.write(("This is the file '" + pathParts[pathParts.length - 1] + "'.").getBytes(), fileForPath);

                pathsToAdd.add(path.substring(1));
            }
        }

        // Add the files in the working copy to version control
        subversionApi.add(dtoFactory.createDto(AddRequest.class).withProjectPath(wcRoot.getAbsolutePath())
                                    .withPaths(pathsToAdd)
                                    .withAddParents(true));

        //Add properties
        final CLIOutputResponse propResponse =
                subversionApi.propset(dtoFactory.createDto(PropertySetRequest.class)
                                                .withValue("user")
                                                .withProjectPath(wcRoot.getAbsolutePath())
                                                .withPath(".")
                                                .withForce(true)
                                                .withDepth(Depth.FULLY_RECURSIVE)
                                                .withName("owner"));

        assertTrue(propResponse.getOutput().size() > 0);

        // Commit the changes
        final CLIOutputWithRevisionResponse cResponse =
                subversionApi.commit(dtoFactory.createDto(CommitRequest.class)
                                               .withProjectPath(wcRoot.getAbsolutePath())
                                               .withMessage("Initial commit."));

        assertEquals(1L, cResponse.getRevision());

        return repoRoot;
    }

    public static <T> Matcher<List<T>> sameAsList(final List<T> expectedList) {
        return new BaseMatcher<List<T>>() {
            @Override
            public void describeTo(final Description description) {
                description.appendText("should contain all and only these elements").appendValue(expectedList);
            }

            @Override
            public boolean matches(final Object o) {
                List<T> actualList;

                try {
                    actualList = (List<T>)o;
                } catch (ClassCastException e) {
                    return false;
                }

                return actualList.containsAll(expectedList);
            }
        };
    }

}
