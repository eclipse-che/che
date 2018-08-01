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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * Implementation of {@link org.eclipse.che.api.core.rest.HttpJsonRequestFactory} that add
 * ```machine.token``` as authorization header. Used to make request from ws-agent to ws-master.
 */
@Singleton
public class AgentHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

  private final String machineToken;

  @Inject
  public AgentHttpJsonRequestFactory(@Named("machine.token") String machineToken) {
    this.machineToken = machineToken;
  }

  @Override
  public HttpJsonRequest fromUrl(@NotNull String url) {
    return super.fromUrl(url).setAuthorizationHeader(getMachineToken());
  }

  @Override
  public HttpJsonRequest fromLink(@NotNull Link link) {
    return super.fromLink(link).setAuthorizationHeader(getMachineToken());
  }

  private String getMachineToken() {
    Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (subject != null && subject.getToken() != null) {
      return subject.getToken();
    } else {
      return machineToken;
    }
  }
}
