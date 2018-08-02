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
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl.jgit;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UserAgent;

/**
 * JGit implementation for GitConnectionFactory
 *
 * @author Tareq Sharafy (tareq.sha@gmail.com)
 */
public class JGitConnectionFactory extends GitConnectionFactory {

  private static final String USER_AGENT = "git/2.1.0";

  private final CredentialsLoader credentialsLoader;
  private final SshKeyProvider sshKeyProvider;
  private final EventService eventService;
  private final GitUserResolver userResolver;

  @Inject
  public JGitConnectionFactory(
      CredentialsLoader credentialsLoader,
      SshKeyProvider sshKeyProvider,
      EventService eventService,
      GitUserResolver userResolver)
      throws GitException {
    this.credentialsLoader = credentialsLoader;
    this.sshKeyProvider = sshKeyProvider;
    this.eventService = eventService;
    this.userResolver = userResolver;

    UserAgent.set(USER_AGENT);
    // Install the all-trusting trust manager
    try {
      SSLContext sslContext = SSLContext.getInstance("SSL");
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts =
          new TrustManager[] {
            new X509TrustManager() {
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
              }

              public void checkClientTrusted(
                  java.security.cert.X509Certificate[] certs, String authType) {}

              public void checkServerTrusted(
                  java.security.cert.X509Certificate[] certs, String authType) {}
            }
          };
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new GitException(e);
    }
  }

  @Override
  public JGitConnection getConnection(File workDir, LineConsumerFactory outputPublisherFactory)
      throws GitException {
    Repository gitRepo = createRepository(workDir);
    JGitConnection conn =
        new JGitConnection(gitRepo, credentialsLoader, sshKeyProvider, eventService, userResolver);
    conn.setOutputLineConsumerFactory(outputPublisherFactory);
    return conn;
  }

  private static Repository createRepository(File workDir) throws GitException {
    try {
      return new FileRepository(new File(workDir, Constants.DOT_GIT));
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  @Override
  public CredentialsLoader getCredentialsLoader() {
    return credentialsLoader;
  }
}
