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
package org.eclipse.che.ide.macro;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.workspace.WsAgentMachineUtil;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

/**
 * Provides dev-machine's host name.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DevMachineHostNameMacro implements Macro {

  private static final String KEY = "${machine.dev.hostname}";

  private final CoreLocalizationConstant localizationConstants;
  private final WsAgentMachineUtil wsAgentMachineUtil;

  @Inject
  public DevMachineHostNameMacro(
      CoreLocalizationConstant localizationConstants, WsAgentMachineUtil wsAgentMachineUtil) {
    this.localizationConstants = localizationConstants;
    this.wsAgentMachineUtil = wsAgentMachineUtil;
  }

  @NotNull
  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroMachineDevHostnameDescription();
  }

  @NotNull
  @Override
  public Promise<String> expand() {
    String value = "";

    Optional<MachineImpl> devMachine = wsAgentMachineUtil.getWsAgentServerMachine();

    if (devMachine.isPresent()) {
      String hostName = devMachine.get().getProperties().get("config.hostname");

      if (hostName != null) {
        value = hostName;
      }
    }

    return Promises.resolve(value);
  }
}
