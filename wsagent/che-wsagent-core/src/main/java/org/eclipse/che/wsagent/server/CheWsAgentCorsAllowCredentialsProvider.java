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
package org.eclipse.che.wsagent.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;

/** Provider of "cors.support.credentials" setting for CORS Filter of WS Agent. True by default */
public class CheWsAgentCorsAllowCredentialsProvider implements Provider<Boolean> {

  private final Boolean allowCredentials;

  @Inject
  public CheWsAgentCorsAllowCredentialsProvider(
      @Nullable @Named("che.wsagent.cors.allow_credentials") String allowCredentials) {
    this.allowCredentials = Boolean.valueOf(allowCredentials);
  }

  @Override
  public Boolean get() {
    return allowCredentials;
  }
}
