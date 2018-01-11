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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.ConflictException;
import org.slf4j.Logger;

/** @author Dmytro Nochevnov */
public class TestGitHubKeyUploader {
  public static final String GITHUB_COM = "github.com";

  private static final Logger LOG = getLogger(TestGitHubKeyUploader.class);

  private final TestGitHubServiceClient testGitHubServiceClient;
  private final TestSshServiceClient testSshServiceClient;
  private final String gitHubUsername;
  private final String gitHubPassword;

  @Inject
  public TestGitHubKeyUploader(
      TestGitHubServiceClient testGitHubServiceClient,
      TestSshServiceClient testSshServiceClient,
      @Named("github.username") String gitHubUsername,
      @Named("github.password") String gitHubPassword) {
    this.testGitHubServiceClient = testGitHubServiceClient;
    this.testSshServiceClient = testSshServiceClient;
    this.gitHubUsername = gitHubUsername;
    this.gitHubPassword = gitHubPassword;
  }

  public synchronized void updateGithubKey() throws Exception {
    testSshServiceClient.deleteVCSKey(GITHUB_COM);

    try {
      String publicKey = testSshServiceClient.generateVCSKey(GITHUB_COM);
      testGitHubServiceClient.uploadPublicKey(
          gitHubUsername, gitHubPassword, publicKey, "Eclipse Che Key");
    } catch (ConflictException e) {
      // ignore if ssh-key for github.com has already existed
      LOG.debug("Ssh key for {} has already existed.", GITHUB_COM);
      return;
    }

    LOG.debug("Ssh key for {} has been generated.", GITHUB_COM);
  }
}
