/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.server;

import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;
import org.eclipse.che.plugin.svn.server.repository.RepositoryUrlProvider;
import org.eclipse.che.plugin.svn.server.utils.TestUtils;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.CheckoutRequest;
import org.eclipse.che.plugin.svn.shared.CopyRequest;
import org.eclipse.che.plugin.svn.shared.Depth;
import org.eclipse.che.plugin.svn.shared.InfoRequest;
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.ListRequest;
import org.eclipse.che.plugin.svn.shared.ListResponse;
import org.eclipse.che.plugin.svn.shared.MoveRequest;
import org.eclipse.che.plugin.svn.shared.PropertyDeleteRequest;
import org.eclipse.che.plugin.svn.shared.PropertySetRequest;
import org.eclipse.che.plugin.svn.shared.SubversionItem;
import org.eclipse.che.plugin.svn.shared.SwitchRequest;
import org.eclipse.che.plugin.svn.shared.UpdateRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link SubversionApi}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubversionApiITest {

    @Mock
    private RepositoryUrlProvider repositoryUrlProvider;
    @Mock
    private SshKeyProvider sshKeyProvider;

    private SubversionApi subversionApi;
    private File          repoRoot;
    private Path          tmpDir;
    private String        repoUrl;
    private String        tmpAbsolutePath;

    @Before
    public void setUp() throws Exception {
        repoRoot = TestUtils.createGreekTreeRepository();
        tmpDir = Files.createTempDirectory(SubversionApiITest.class.getName() + "-");
        repoUrl = Paths.get(repoRoot.getAbsolutePath()).toUri().toString();
        tmpAbsolutePath = tmpDir.toFile().getAbsolutePath();
        tmpDir.toFile().deleteOnExit();

        this.subversionApi = new SubversionApi(repositoryUrlProvider, new SshScriptProvider(sshKeyProvider));
    }

    /**
     * Tests for {@link SubversionApi#checkout(CheckoutRequest)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    public void testCheckout() throws Exception {
        CLIOutputWithRevisionResponse response =
                this.subversionApi.checkout(DtoFactory.getInstance()
                                                      .createDto(CheckoutRequest.class)
                                                      .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                      .withUrl(repoUrl));

        assertTrue(response.getRevision() > -1);
    }

    @Test
    public void testSwitchToBranch() throws Exception {
        subversionApi.checkout(DtoFactory.getInstance()
                                         .createDto(CheckoutRequest.class)
                                         .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                         .withUrl(repoUrl + "/trunk"));

        CLIOutputWithRevisionResponse response = subversionApi.doSwitch(DtoFactory.getInstance()
                                                                                  .createDto(SwitchRequest.class)
                                                                                  .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                                                  .withLocation("^/branches/2.0")
                                                                                  .withIgnoreAncestry(true));

        assertTrue(response.getRevision() > -1);
    }

    @Test
    public void testSwitchToTag() throws Exception {
        subversionApi.checkout(DtoFactory.getInstance()
                                         .createDto(CheckoutRequest.class)
                                         .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                         .withUrl(repoUrl + "/trunk"));

        CLIOutputWithRevisionResponse response = this.subversionApi.doSwitch(DtoFactory.getInstance()
                                                                                       .createDto(SwitchRequest.class)
                                                                                       .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                                                       .withLocation("^/tags/1.0")
                                                                                       .withIgnoreAncestry(true));

        assertTrue(response.getRevision() > -1);

    }

    @Test
    public void testSwitchToTrunk() throws Exception {
        subversionApi.checkout(DtoFactory.getInstance()
                                         .createDto(CheckoutRequest.class)
                                         .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                         .withUrl(repoUrl + "/tags/1.0"));


        CLIOutputWithRevisionResponse response = this.subversionApi.doSwitch(DtoFactory.getInstance()
                                                                                       .createDto(SwitchRequest.class)
                                                                                       .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                                                       .withLocation("^/trunk")
                                                                                       .withIgnoreAncestry(true));

        assertTrue(response.getRevision() > -1);
    }

    @Test
    public void testListBranches() throws Exception {
        subversionApi.checkout(DtoFactory.getInstance()
                                         .createDto(CheckoutRequest.class)
                                         .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                         .withUrl(repoUrl + "/trunk"));

        ListResponse response = subversionApi.list(DtoFactory.getInstance()
                                                             .createDto(ListRequest.class)
                                                             .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                             .withTargetPath("^/branches"));
        List<String> output = response.getOutput();
        assertEquals(output.size(), 1);
        assertEquals(output.get(0), "2.0/");

        response = subversionApi.listBranches(DtoFactory.getInstance()
                                                        .createDto(ListRequest.class)
                                                        .withProjectPath(tmpDir.toFile().getAbsolutePath()));

        output = response.getOutput();
        assertEquals(output.size(), 1);
        assertEquals(output.get(0), "2.0");
    }

    @Test
    public void testListTags() throws Exception {
        subversionApi.checkout(DtoFactory.getInstance()
                                         .createDto(CheckoutRequest.class)
                                         .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                         .withUrl(repoUrl));

        ListResponse response = subversionApi.list(DtoFactory.getInstance()
                                                             .createDto(ListRequest.class)
                                                             .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                             .withTargetPath("^/tags"));
        List<String> output = response.getOutput();
        assertEquals(output.size(), 1);
        assertEquals(output.get(0), "1.0/");

        response = subversionApi.listTags(DtoFactory.getInstance()
                                                    .createDto(ListRequest.class)
                                                    .withProjectPath(tmpDir.toFile().getAbsolutePath()));

        output = response.getOutput();
        assertEquals(output.size(), 1);
        assertEquals(output.get(0), "1.0");
    }

    /**
     * Tests for {@link SubversionApi#copy(CopyRequest)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    public void testCopy() throws Exception {
        this.subversionApi.checkout(DtoFactory.getInstance()
                                              .createDto(CheckoutRequest.class)
                                              .withProjectPath(tmpAbsolutePath)
                                              .withUrl(repoUrl));

        CLIOutputResponse response = this.subversionApi.copy(DtoFactory.getInstance()
                                                                       .createDto(CopyRequest.class)
                                                                       .withProjectPath(tmpAbsolutePath)
                                                                       .withSource("trunk/A/B/lambda")
                                                                       .withDestination("trunk/A/B/E/lambda"));
        assertEquals(response.getOutput().size(), 1);
        assertTrue(response.getErrOutput().isEmpty());

        String expectedPath = "trunk" + File.separator + "A" + File.separator + "B" + File.separator + "E" + File.separator + "lambda";
        assertEquals(response.getOutput().get(0), "A         " + expectedPath);
    }

    /**
     * Tests for {@link SubversionApi#move(MoveRequest)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    public void testMove() throws Exception {
        this.subversionApi.checkout(DtoFactory.getInstance()
                                              .createDto(CheckoutRequest.class)
                                              .withProjectPath(tmpAbsolutePath)
                                              .withUrl(repoUrl));

        CLIOutputResponse response = this.subversionApi.move(DtoFactory.getInstance()
                                                                       .createDto(MoveRequest.class)
                                                                       .withProjectPath(tmpAbsolutePath)
                                                                       .withSource(Collections.singletonList("trunk/A/B/lambda"))
                                                                       .withDestination("trunk/A/B/E/lambda"));
        assertEquals(response.getOutput().size(), 2);
        assertTrue(response.getErrOutput().isEmpty());

        String expectedPath1 = "trunk" + File.separator + "A" + File.separator + "B" + File.separator + "E" + File.separator + "lambda";
        assertEquals(response.getOutput().get(0), "A         " + expectedPath1);

        String expectedPath2 = "trunk" + File.separator + "A" + File.separator + "B" + File.separator + "lambda";
        assertEquals(response.getOutput().get(1), "D         " + expectedPath2);
    }

    /**
     * Tests for {@link SubversionApi#exportPath(String, String, String)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testExport() throws Exception {
        this.subversionApi.checkout(DtoFactory.getInstance()
                                              .createDto(CheckoutRequest.class)
                                              .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                              .withUrl(repoUrl));
        Response response = this.subversionApi.exportPath(tmpDir.toFile().getAbsolutePath(), "trunk/A/B", null);

        Collection<String> items = ZipUtils.listEntries((InputStream)response.getEntity());
        assertEquals(items.size(), 3);
    }

    /**
     * Tests for {@link SubversionApi#propset(org.eclipse.che.plugin.svn.shared.PropertySetRequest)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    public void testPropSet() throws Exception {
        this.subversionApi.checkout(DtoFactory.getInstance()
                                              .createDto(CheckoutRequest.class)
                                              .withProjectPath(tmpAbsolutePath)
                                              .withUrl(repoUrl));

        CLIOutputResponse response = this.subversionApi.propset(DtoFactory.getInstance().createDto(PropertySetRequest.class)
                                                                          .withValue("'*.*'")
                                                                          .withProjectPath(tmpAbsolutePath)
                                                                          .withPath("trunk/A/B")
                                                                          .withForce(true)
                                                                          .withDepth(Depth.DIRS_ONLY)
                                                                          .withName("svn:ignore"));

        assertEquals(response.getOutput().size(), 1);
        assertEquals(response.getOutput().get(0), "property 'svn:ignore' set on 'trunk" + File.separator + "A" + File.separator + "B'");
    }

    /**
     * Tests for {@link SubversionApi#propdel(org.eclipse.che.plugin.svn.shared.PropertyDeleteRequest)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    public void testPropDel() throws Exception {
        this.subversionApi.checkout(DtoFactory.getInstance()
                                              .createDto(CheckoutRequest.class)
                                              .withProjectPath(tmpAbsolutePath)
                                              .withUrl(repoUrl));

        CLIOutputResponse response = this.subversionApi.propdel(DtoFactory.getInstance().createDto(PropertyDeleteRequest.class)
                                                                          .withProjectPath(tmpAbsolutePath)
                                                                          .withPath("trunk/A/B")
                                                                          .withForce(true)
                                                                          .withDepth(Depth.DIRS_ONLY)
                                                                          .withName("owner"));

        assertEquals(response.getOutput().size(), 1);
        assertEquals(response.getOutput().get(0), "property 'owner' deleted from 'trunk" + File.separator + "A" + File.separator + "B'.");
    }

    /**
     * Tests for {@link SubversionApi#update(UpdateRequest)}.
     *
     * @throws Exception
     *         if anything goes wrong
     */
    @Test
    public void testUpdate() throws Exception {
        final long coRevision = this.subversionApi.checkout(DtoFactory.getInstance()
                                                                      .createDto(CheckoutRequest.class)
                                                                      .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                                      .withUrl(repoUrl)
                                                                      .withDepth("immediates"))
                                                  .getRevision();
        final CLIOutputWithRevisionResponse response =
                this.subversionApi.update(DtoFactory.getInstance()
                                                    .createDto(UpdateRequest.class)
                                                    .withProjectPath(tmpDir.toFile().getAbsolutePath()));

        assertTrue(coRevision > -1);
        assertTrue(response.getRevision() > -1);

        assertTrue(coRevision <= response.getRevision());
    }

    @Test
    public void testInfo() throws Exception {
        subversionApi.checkout(DtoFactory.getInstance()
                                         .createDto(CheckoutRequest.class)
                                         .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                         .withUrl(repoUrl)
                                         .withDepth("immediates"));

        InfoResponse response = subversionApi.info(DtoFactory.getInstance()
                                                             .createDto(InfoRequest.class)
                                                             .withProjectPath(tmpDir.toFile().getAbsolutePath())
                                                             .withTarget(".")
                                                             .withRevision("HEAD"));
        assertEquals(response.getItems().size(), 1);

        SubversionItem subversionItem = response.getItems().get(0);
        assertEquals(subversionItem.getProjectUri(), repoUrl.substring(0, repoUrl.length() - 1));
    }
}
