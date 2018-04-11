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
import org.apache.commons.io.IOUtils;
import org.eclipse.che.commons.lang.NameGenerator;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is facade and helper for {@link GHRepository}.
 *
 * @author Dmytro Nochevnov
 */
public class TestGitHubRepository {

  private static final int GITHUB_OPERATION_TIMEOUT_SEC = 1;
  private static final int REPO_CREATION_ATTEMPTS = 6;

  private final String repoName = NameGenerator.generate("EclipseCheTestRepo-", 5);
  private static final Logger LOG = LoggerFactory.getLogger(TestGitHubRepository.class);

  private GHRepository ghRepo;
  private final GitHub gitHub;

  /**
   * Creates repository with semi-random name on GitHub for certain {@code gitHubUsername}. Waits
   * until repository is really created.
   *
   * @param gitHubUsername default github user name
   * @param gitHubPassword default github user password
   * @throws IOException
   * @throws InterruptedException
   */
  @Inject
  public TestGitHubRepository(
      @Named("github.username") String gitHubUsername,
      @Named("github.password") String gitHubPassword)
      throws IOException, InterruptedException {
    gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword);
    ghRepo = create();
  }

  public String getName() {
    return repoName;
  }

  public String getSha1(String branchName) throws IOException {
    return ghRepo.getBranch(branchName).getSHA1();
  }

  /**
   * Creates reference to the new branch with {@code branch} from default branch.
   *
   * @param branchName name of the branch which should be created
   * @return reference to the new branch
   * @throws IOException
   */
  public GHRef createBranch(String branchName) throws IOException {
    GHRef defaultBranch = getReferenceToDefaultBranch();
    return ghRepo.createRef("refs/heads/" + branchName, defaultBranch.getObject().getSha());
  }

  /**
   * Creates reference to the new tag with {@code tagName} from default branch.
   *
   * @param tagName is a name of new tag
   * @return reference to the new tag
   * @throws IOException
   */
  public GHRef createTag(String tagName) throws IOException {
    GHRef defaultBranch = getReferenceToDefaultBranch();
    return ghRepo.createRef("refs/tags/" + tagName, defaultBranch.getObject().getSha());
  }

  private GHRef getReferenceToDefaultBranch() throws IOException {
    return ghRepo.getRef("heads/" + ghRepo.getDefaultBranch());
  }

  public void setDefaultBranch(String branchName) throws IOException {
    ghRepo.setDefaultBranch(branchName);
    ghRepo = gitHub.getRepository(ghRepo.getFullName());
  }

  /**
   * Copies content of directory {@code pathToRootContentDirectory} to the GitHub repository. It
   * tries to recreate the file ones again in case of FileNotFoundException occurs.
   *
   * @param pathToRootContentDirectory path to the directory with content
   * @throws IOException
   */
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

  /**
   * Delete folder with content inside the repository on GitHub.
   *
   * @param folder folder to delete
   * @param deleteCommitMessage commit message which is used to delete the message
   * @throws IOException
   */
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

  /**
   * Creates file in GitHub repository.
   *
   * @param pathToRootContentDirectory path to the root directory of file locally
   * @param pathToFile path to file locally
   * @throws IOException
   */
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

  public String getFileContent(String pathToFile) throws IOException {
    return IOUtils.toString(ghRepo.getFileContent(pathToFile).read(), "UTF-8");
  }
}
