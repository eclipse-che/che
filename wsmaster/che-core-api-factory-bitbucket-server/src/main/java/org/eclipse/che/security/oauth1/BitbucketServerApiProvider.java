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
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketServerApi;
import org.eclipse.che.api.factory.server.bitbucket.server.HttpBitbucketServerApi;
import org.eclipse.che.api.factory.server.bitbucket.server.NopBitbucketServerApi;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BitbucketServerApiProvider implements Provider<BitbucketServerApi> {
  private final BitbucketServerApi bitbucketServerApi;
  private static final Logger LOG = LoggerFactory.getLogger(BitbucketServerApiProvider.class);

  @Inject
  public BitbucketServerApiProvider(
      @Nullable @Named("che.integration.bitbucket.server_endpoints") String bitbucketEndpoints,
      @Nullable @Named("che.oauth1.bitbucket.endpoint") String bitbucketOauth1Endpoint,
      Set<OAuthAuthenticator> authenticators) {
    bitbucketServerApi = doGet(bitbucketEndpoints, bitbucketOauth1Endpoint, authenticators);
    LOG.debug("Bitbucket server api is used {}", bitbucketServerApi);
  }

  @Override
  public BitbucketServerApi get() {
    return bitbucketServerApi;
  }

  private static BitbucketServerApi doGet(
      String bitbucketEndpoints,
      String bitbucketOauth1Endpoint,
      Set<OAuthAuthenticator> authenticators) {
    if (isNullOrEmpty(bitbucketOauth1Endpoint)) {
      return new NopBitbucketServerApi();
    } else {
      if (isNullOrEmpty(bitbucketEndpoints)) {
        throw new RuntimeException(
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
            throw new RuntimeException(
                "'che.oauth1.bitbucket.endpoint' is set but BitbucketServerOAuthAuthenticator deployed correctly");
          }
          return new HttpBitbucketServerApi(
              bitbucketOauth1Endpoint,
              new BitbucketServerOAuth1AuthorizationHeaderSupplier(
                  (BitbucketServerOAuthAuthenticator) authenticator.get()));
        } else {
          throw new RuntimeException(
              "`che.integration.bitbucket.server_endpoints` must contain `"
                  + bitbucketOauth1Endpoint
                  + "` value");
        }
      }
    }
  }
}
