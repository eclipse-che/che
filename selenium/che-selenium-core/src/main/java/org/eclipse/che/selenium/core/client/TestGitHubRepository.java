/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.PreDestroy;
import org.eclipse.che.commons.lang.NameGenerator;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
public class TestGitHubRepository {

  private static final int GITHUB_OPERATION_TIMEOUT_SEC = 1;
  private static final int REPO_CREATION_ATTEMPTS = 6;

  private final String repoName = NameGenerator.generate("EclipseCheTestRepo-", 5);
  private static final Logger LOG = LoggerFactory.getLogger(TestGitHubRepository.class);

  private final GHRepository ghRepo;
  private final GitHub gitHub;

  @Inject
  public TestGitHubRepository(
      @Named("github.username") String gitHubUsername,
      @Named("github.password") String gitHubPassword)
      throws IOException, InterruptedException {
    gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword);
    ghRepo = create();
  }

  private GHRepository create() throws IOException, InterruptedException {
    GHRepository repo = gitHub.createRepository(repoName).create();
    ensureRepositoryCreated(repo, System.currentTimeMillis());

    LOG.info("GitHub repo {} has been created", repo.getHtmlUrl());
    return repo;
  }

  private void ensureRepositoryCreated(GHRepository repo, long startCreationTimeInMillisec)
      throws IOException {
    Throwable lastIOException = null;
    for (int i = 0; i < REPO_CREATION_ATTEMPTS; i++) {
      try {
        gitHub.getRepository(repo.getFullName());
        return;
      } catch (IOException e) {
        lastIOException = e;
        LOG.info("Waiting for {} to be created", repo.getHtmlUrl());
        sleepQuietly(GITHUB_OPERATION_TIMEOUT_SEC); // sleep one second
      }
    }

    long durationOfRepoCreationInSec =
        (System.currentTimeMillis() - startCreationTimeInMillisec) / 1000;

    throw new IOException(
        format(
            "GitHub repo %s hasn't been created in %s seconds",
            repo.getHtmlUrl(), durationOfRepoCreationInSec),
        lastIOException);
  }

  public String getName() {
    return repoName;
  }

  /**
   * Creates reference to branch, tag, ... from master branch.
   *
   * @param refName is a name of branch, tag, etc
   * @throws IOException
   */
  public GHRef addRefFromMaster(String refName) throws IOException {
    GHRef master = ghRepo.getRef("heads/master");
    return ghRepo.createRef("refs/heads/" + refName, master.getObject().getSha());
  }

  public void addContent(Path pathToRootContentDirectory) throws IOException {
    Files.walk(pathToRootContentDirectory)
        .filter(Files::isRegularFile)
        .forEach(
            pathToFile -> {
              try {
                createFile(pathToRootContentDirectory, pathToFile);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
  }

  private void createFile(Path pathToRootContentDirectory, Path pathToFile) throws IOException {
    byte[] contentBytes = Files.readAllBytes(pathToFile);
    String relativePath = pathToRootContentDirectory.relativize(pathToFile).toString();
    String commitMessage = String.format("Add file %s", relativePath);

    try {
      ghRepo.createContent(contentBytes, commitMessage, relativePath);
    } catch (GHFileNotFoundException e) {
      // try to create content once again
      LOG.warn(
          "Error of creation of {} occurred. Is trying to create it once again...",
          ghRepo.getHtmlUrl() + "/" + relativePath);
      sleepQuietly(GITHUB_OPERATION_TIMEOUT_SEC);
      ghRepo.createContent(contentBytes, commitMessage, relativePath);
    }
  }

  /**
   * Changes content of the file
   *
   * @param pathToFile path to specified file
   * @param content content to change
   * @throws IOException
   */
  public void changeFileContent(String pathToFile, String content) throws IOException {
    changeFileContent(pathToFile, content, format("Change file %s", pathToFile));
  }

  /**
   * Changes content of the file
   *
   * @param pathToFile path to specified file
   * @param content content to change
   * @param commitMessage message to commit
   * @throws IOException
   */
  public void changeFileContent(String pathToFile, String content, String commitMessage)
      throws IOException {
    ghRepo.getFileContent(String.format("/%s", pathToFile)).update(content, commitMessage);
  }

  public void deleteFile(String pathToFile) throws IOException {
    ghRepo.getFileContent(pathToFile).delete("Delete file " + pathToFile);
  }

  public void deleteFolder(Path folder, String deleteCommitMessage) throws IOException {
    for (GHContent ghContent : ghRepo.getDirectoryContent(folder.toString())) {
      ghContent.delete(deleteCommitMessage);
    }
  }

  @PreDestroy
  public void delete() throws IOException {
    ghRepo.delete();
    LOG.info("GitHub repo {} has been removed", ghRepo.getHtmlUrl());
  }

  public String getHtmlUrl() {
    return ghRepo.getHtmlUrl().toString();
  }

  public String getSshUrl() {
    return ghRepo.getSshUrl();
  }
}
