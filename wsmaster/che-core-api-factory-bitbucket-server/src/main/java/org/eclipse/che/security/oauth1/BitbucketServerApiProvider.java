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

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketServerApiClient;
import org.eclipse.che.api.factory.server.bitbucket.server.HttpBitbucketServerApiClient;
import org.eclipse.che.api.factory.server.bitbucket.server.NoopBitbucketServerApiClient;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.inject.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BitbucketServerApiProvider implements Provider<BitbucketServerApiClient> {

  private static final Logger LOG = LoggerFactory.getLogger(BitbucketServerApiProvider.class);

  private final BitbucketServerApiClient bitbucketServerApiClient;

  @Inject
  public BitbucketServerApiProvider(
      @Nullable @Named("che.integration.bitbucket.server_endpoints") String bitbucketEndpoints,
      @Nullable @Named("che.oauth1.bitbucket.endpoint") String bitbucketOauth1Endpoint,
      Set<OAuthAuthenticator> authenticators) {
    bitbucketServerApiClient = doGet(bitbucketEndpoints, bitbucketOauth1Endpoint, authenticators);
    LOG.debug("Bitbucket server api is used {}", bitbucketServerApiClient);
  }

  @Override
  public BitbucketServerApiClient get() {
    return bitbucketServerApiClient;
  }

  private static BitbucketServerApiClient doGet(
      String bitbucketEndpoints,
      String bitbucketOauth1Endpoint,
      Set<OAuthAuthenticator> authenticators) {
    if (isNullOrEmpty(bitbucketOauth1Endpoint)) {
      return new NoopBitbucketServerApiClient();
    } else {
      if (isNullOrEmpty(bitbucketEndpoints)) {
        throw new ConfigurationException(
            "`che.integration.bitbucket.server_endpoints` bitbucket configuration is missing."
                + " It should contain values from 'che.oauth1.bitbucket.endpoint'");
      } else {
        if (bitbucketEndpoints.contains(bitbucketOauth1Endpoint)) {
          Optional<OAuthAuthenticator> authenticator =
              authenticators
                  .stream()
                  .filter(
                      a ->
                          a.getOAuthProvider()
                              .equals(BitbucketServerOAuthAuthenticator.AUTHENTICATOR_NAME))
                  .filter(
                      a -> BitbucketServerOAuthAuthenticator.class.isAssignableFrom(a.getClass()))
                  .findFirst();
          if (authenticator.isEmpty()) {
            throw new ConfigurationException(
                "'che.oauth1.bitbucket.endpoint' is set but BitbucketServerOAuthAuthenticator is not deployed correctly");
          }
          return new HttpBitbucketServerApiClient(
              bitbucketOauth1Endpoint, (BitbucketServerOAuthAuthenticator) authenticator.get());
        } else {
          throw new ConfigurationException(
              "`che.integration.bitbucket.server_endpoints` must contain `"
                  + bitbucketOauth1Endpoint
                  + "` value");
        }
      }
    }
  }
}
