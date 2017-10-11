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
package org.eclipse.che.multiuser.machine.authentication.ide;

import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;

import com.google.inject.Singleton;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.ide.api.machine.CheWsAgentLinksModifier;
import org.eclipse.che.ide.api.machine.DevMachine;

/**
 * Inserts in each URL machine token.
 *
 * @author Anton Korneta
 */
@Singleton
public class CheAuthMachineLinksModifier extends CheWsAgentLinksModifier {
  private static final String MACHINE_TOKEN = "token";

  private String machineToken;

  @Override
  public void initialize(DevMachine devMachine) {
    Link link = devMachine.getMachineLink(WSAGENT_WEBSOCKET_REFERENCE);
    if (link != null) {
      for (LinkParameter parameter : link.getParameters()) {
        if (MACHINE_TOKEN.equals(parameter.getName())) {
          machineToken = parameter.getDefaultValue();
        }
      }
    }
  }

  @Override
  public String modify(String agentUrl) {
    if (machineToken != null) {
      return agentUrl + (agentUrl.contains("?") ? '&' : '?') + "token=" + machineToken;
    }
    throw new RuntimeException("Failed to modify url, machine token in not specified");
  }
}
