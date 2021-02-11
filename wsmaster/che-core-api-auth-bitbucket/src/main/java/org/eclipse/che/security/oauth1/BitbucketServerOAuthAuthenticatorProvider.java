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
package org.eclipse.che.security.oauth1;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BitbucketServerOAuthAuthenticatorProvider implements Provider<OAuthAuthenticator> {
  private static final Logger LOG =
      LoggerFactory.getLogger(BitbucketServerOAuthAuthenticatorProvider.class);

  private final OAuthAuthenticator authenticator;

  @Inject
  public BitbucketServerOAuthAuthenticatorProvider(
      @Nullable @Named("che.oauth1.bitbucket.consumerkeypath") String consumerKeyPath,
      @Nullable @Named("che.oauth1.bitbucket.privatekeypath") String privateKeyPath,
      @Nullable @Named("che.oauth1.bitbucket.endpoint") String bitbucketEndpoint,
      @Named("che.api") String apiEndpoint)
      throws IOException {
    authenticator =
        getOAuthAuthenticator(consumerKeyPath, privateKeyPath, bitbucketEndpoint, apiEndpoint);
    LOG.debug("{} Bitbucket OAuthAuthenticator is used.", authenticator);
  }

  @Override
  public OAuthAuthenticator get() {
    return authenticator;
  }

  private static OAuthAuthenticator getOAuthAuthenticator(
      String consumerKeyPath, String privateKeyPath, String bitbucketEndpoint, String apiEndpoint)
      throws IOException {
    if (!isNullOrEmpty(bitbucketEndpoint)
        && !isNullOrEmpty(consumerKeyPath)
        && !isNullOrEmpty(privateKeyPath)) {
      String consumerKey = Files.readString(Path.of(consumerKeyPath));
      String privateKey = Files.readString(Path.of(privateKeyPath));
      if (!isNullOrEmpty(consumerKey) && !isNullOrEmpty(privateKey)) {
        return new BitbucketServerOAuthAuthenticator(
            consumerKey, privateKey, bitbucketEndpoint, apiEndpoint);
      }
    }

    return new NoopOAuthAuthenticator();
  }
}
