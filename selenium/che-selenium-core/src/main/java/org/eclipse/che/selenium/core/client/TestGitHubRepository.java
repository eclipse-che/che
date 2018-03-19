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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.lang.NameGenerator;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
public class TestGitHubRepository {
  private final String repoName = NameGenerator.generate("EclipseCheTestRepo-", 5);
  private static final Logger LOG = LoggerFactory.getLogger(TestGitHubRepository.class);

  private final GHRepository ghRepository;
  private final GitHub gitHub;

  @Inject
  public TestGitHubRepository(
      @Named("github.username") String gitHubUsername,
      @Named("github.password") String gitHubPassword)
      throws IOException, InterruptedException {
    gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword);
    ghRepository = create();
  }

  private GHRepository create() throws IOException, InterruptedException {
    GHRepository repo = gitHub.createRepository(repoName).create();

    // ensure repository exists
    for (int attempts = 0; attempts < 5; attempts++) {
      try {
        repo = gitHub.getRepository(repo.getFullName());
        break;
      } catch (Exception e) {
        LOG.info("Waiting for {} to be created", repo.getFullName());
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      }
    }

    LOG.info("GitHub repo {} has been created", repo.getHtmlUrl());

    return repo;
  }

  public String getName() {
    return repoName;
  }

  public void addContent(Path localRepo, String commitMessage) throws IOException {
    Files.walk(localRepo)
        .filter(Files::isRegularFile)
        .forEach(
            path -> {
              try {
                byte[] contentBytes = Files.readAllBytes(path);
                String relativePath = localRepo.relativize(path).toString();
                ghRepository.createContent(contentBytes, commitMessage, relativePath);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
  }

  public void delete(Path folder, String commitMessage) throws IOException {
    for (GHContent ghContent : ghRepository.getDirectoryContent(folder.toString())) {
      ghContent.delete(commitMessage);
    }
  }

  public void delete() throws IOException {
    ghRepository.delete();
    LOG.info("GitHub repo {} has been removed", ghRepository.getHtmlUrl());
  }

  public GHContent getFileContent(String path) throws IOException {
    return ghRepository.getFileContent(path);
  }

  public String getHtmlUrl() {
    return ghRepository.getHtmlUrl().toString();
  }

  public String getSshUrl() {
    return ghRepository.getSshUrl();
  }
}
