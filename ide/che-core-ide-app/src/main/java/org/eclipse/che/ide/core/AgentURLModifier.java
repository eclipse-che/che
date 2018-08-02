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
package org.eclipse.che.ide.core;

import com.google.inject.Singleton;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

/**
 * Inserts in each URL machine token.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Singleton
public class AgentURLModifier {

  private final AppContext appContext;

  @Inject
  public AgentURLModifier(AppContext appContext) {
    this.appContext = appContext;
  }

  /**
   * Changes source url by adding of machine token.
   *
   * @param agentUrl any url to the agent
   * @return modified url
   */
  public String modify(String agentUrl) {
    Optional<String> machineToken = getMachineToken();
    return machineToken
        .map(s -> agentUrl + (agentUrl.contains("?") ? '&' : '?') + "token=" + s)
        .orElse(agentUrl);
  }

  private Optional<String> getMachineToken() {
    WorkspaceImpl currentWorkspace = appContext.getWorkspace();
    if (currentWorkspace == null) {
      return Optional.empty();
    }

    RuntimeImpl runtime = currentWorkspace.getRuntime();
    if (runtime == null) {
      return Optional.empty();
    }

    String machineToken = runtime.getMachineToken();
    return Optional.of(machineToken);
  }
}
