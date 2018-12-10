/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link GitConfigurationChecker}.
 *
 * @author Artem Zatsarynnyi
 */
public class GitConfigurationCheckerTest {
  private static final String GITIGNORE_FILE_CONTENT =
      "\n" + "# Codenvy files\n" + ".che/\n" + ".vfs/\n";
  private GitConfigurationChecker checker;
  private static String excludesfilePropertyContent;
  private static Path globalGitconfigFilePath;
  private static Path gitignoreFilePath;
  private static Path existingGitignoreFilePath;

  @BeforeClass
  public static void init() throws Exception {
    Path targetDir =
        Paths.get(Thread.currentThread().getContextClassLoader().getResource(".").toURI())
            .getParent();
    globalGitconfigFilePath = targetDir.resolve(".gitconfig");
    gitignoreFilePath = targetDir.resolve(".gitignore_codenvy");
    existingGitignoreFilePath = targetDir.resolve(".existing_gitignore_file");
  }

  @Before
  public void setUp() throws Exception {
    checker = new GitConfigurationChecker(globalGitconfigFilePath, gitignoreFilePath);
  }

  @After
  public void tearDown() throws Exception {
    Files.deleteIfExists(globalGitconfigFilePath);
    Files.deleteIfExists(gitignoreFilePath);
    Files.deleteIfExists(existingGitignoreFilePath);
  }

  @Test
  public void testWhenNoGitconfigFile() throws Exception {
    excludesfilePropertyContent =
        String.format("\n" + "[core]\n" + "\texcludesfile = %s" + "\n", gitignoreFilePath);
    checker.start();

    Assert.assertTrue(
        "New global .gitconfig file should be created in case it doesn't exist.",
        Files.exists(globalGitconfigFilePath));
    Assert.assertTrue(
        "New global .gitignore file should be created.", Files.exists(gitignoreFilePath));
    Assert.assertEquals(
        excludesfilePropertyContent, new String(Files.readAllBytes(globalGitconfigFilePath)));
    Assert.assertEquals(GITIGNORE_FILE_CONTENT, new String(Files.readAllBytes(gitignoreFilePath)));
  }

  @Test
  public void testWhenNoExcludesfilePropertyInGitconfigFile() throws Exception {
    excludesfilePropertyContent =
        String.format("\n" + "[core]\n" + "\texcludesfile = %s" + "\n", gitignoreFilePath);
    createGitconfigFile(false);
    final String existingGitconfigFileContent =
        new String(Files.readAllBytes(globalGitconfigFilePath));
    checker.start();

    Assert.assertTrue(Files.exists(globalGitconfigFilePath));
    Assert.assertEquals(
        "'core.excludesfile' property should be appended to the existing global .gitconfig file",
        existingGitconfigFileContent + excludesfilePropertyContent,
        new String(Files.readAllBytes(globalGitconfigFilePath)));
    Assert.assertEquals(GITIGNORE_FILE_CONTENT, new String(Files.readAllBytes(gitignoreFilePath)));
  }

  @Test
  public void testWithExcludesfilePropertyInGitconfigFile() throws Exception {
    excludesfilePropertyContent =
        String.format("\n" + "[core]\n" + "\texcludesfile = %s" + "\n", existingGitignoreFilePath);
    createGitconfigFile(true);
    final byte[] existingGitconfigFileContent = Files.readAllBytes(globalGitconfigFilePath);
    final String existingGitignoreFileContent =
        new String(Files.readAllBytes(existingGitignoreFilePath));
    checker.start();

    Assert.assertArrayEquals(
        "Existing global .gitconfig file shouldn't be touched in case it already contains 'core.excludesfile' property.",
        existingGitconfigFileContent,
        Files.readAllBytes(globalGitconfigFilePath));
    Assert.assertFalse(
        "New .gitignore file shouldn't be created in case existing global .gitconfig file already contains 'core.excludesfile' property.",
        Files.exists(gitignoreFilePath));
    Assert.assertEquals(
        "New content should be appended to the existing global .gitignore file.",
        existingGitignoreFileContent + GITIGNORE_FILE_CONTENT,
        new String(Files.readAllBytes(existingGitignoreFilePath)));
  }

  private static void createGitconfigFile(boolean withExcludesfileProperty) throws Exception {
    String gitconfigFileContent = "some existed content\n";
    if (withExcludesfileProperty) {
      Files.write(existingGitignoreFilePath, "some content\n".getBytes());
      gitconfigFileContent = gitconfigFileContent + excludesfilePropertyContent;
    }
    Files.write(globalGitconfigFilePath, gitconfigFileContent.getBytes());
  }
}
