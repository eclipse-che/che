/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Map;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.WorkspacesRoutingSuffixProvider;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;

/**
 * This server evaluation strategy will return a completed {@link ServerImpl} with internal
 * addresses and external addresses managed by the `custom` evaluation strategy,and its template
 * property `che.docker.server_evaluation_strategy.custom.template`
 *
 * <p>cheExternalAddress can be set using property {@code che.docker.ip.external}. This strategy is
 * useful when Che and the workspace servers need to be exposed on the same single TCP port
 *
 * @author David Festal <dfestal@redhat.com>
 * @see ServerEvaluationStrategy
 */
public class AlwaysExternalCustomServerEvaluationStrategy extends BaseServerEvaluationStrategy {

  @Inject
  public AlwaysExternalCustomServerEvaluationStrategy(
      @Nullable @Named("che.docker.ip") String internalAddress,
      @Nullable @Named("che.docker.ip.external") String externalAddress,
      @Nullable @Named("che.docker.server_evaluation_strategy.custom.template")
          String cheDockerCustomExternalTemplate,
      @Nullable @Named("che.docker.server_evaluation_strategy.custom.external.protocol")
          String cheDockerCustomExternalProtocol,
      @Named("che.port") String chePort,
      WorkspacesRoutingSuffixProvider cheWorkspacesRoutingSuffixProvider) {
    super(
        internalAddress,
        externalAddress,
        cheDockerCustomExternalTemplate,
        cheDockerCustomExternalProtocol,
        chePort,
        true,
        cheWorkspacesRoutingSuffixProvider);
  }

  @Override
  protected Map<String, String> getInternalAddressesAndPorts(
      ContainerInfo containerInfo, String internalHost) {
    return super.getExternalAddressesAndPorts(containerInfo, internalHost);
  }
}
