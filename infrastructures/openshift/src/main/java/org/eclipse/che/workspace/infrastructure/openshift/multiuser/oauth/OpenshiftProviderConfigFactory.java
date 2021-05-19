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
package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates {@link OpenShiftConfig} from given OpenShift token. It does not guarantee that
 * token is valid.
 */
@Singleton
public class OpenshiftProviderConfigFactory extends OpenShiftClientConfigFactory {
  private static final Logger LOG = LoggerFactory.getLogger(OpenshiftProviderConfigFactory.class);

  @Override
  public boolean isPersonalized() {
    return true;
  }

  /**
   * Builds the OpenShift {@link Config} object based on a default {@link Config} object and given
   * 'token'. It ignores 'workspaceId'.
   *
   * <p>'token' can be passed in plain format or with 'Bearer ' prefix, when used from http headers.
   */
  public Config buildConfig(
      Config defaultConfig, @Nullable String workspaceId, @Nullable String token) {
    if (token != null) {
      LOG.debug("Creating token authenticated client");
      if (token.toLowerCase().startsWith("bearer")) {
        token = token.substring("Bearer ".length());
      }
      return new OpenShiftConfigBuilder(OpenShiftConfig.wrap(defaultConfig))
          .withOauthToken(token)
          .build();
    } else {
      LOG.debug("NO TOKEN PASSED. Getting default client config.");
      return defaultConfig;
    }
  }
}
