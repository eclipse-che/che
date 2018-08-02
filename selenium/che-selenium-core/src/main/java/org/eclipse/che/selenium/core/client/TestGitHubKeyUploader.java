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
package org.eclipse.che.selenium.core.client;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.ConflictException;
import org.slf4j.Logger;

/** @author Dmytro Nochevnov */
@Singleton
public class TestGitHubKeyUploader {
  public static final String GITHUB_COM = "github.com";

  private static final Logger LOG = getLogger(TestGitHubKeyUploader.class);

  @Inject private TestGitHubServiceClient testGitHubServiceClient;

  @Inject private TestSshServiceClient testSshServiceClient;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

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
