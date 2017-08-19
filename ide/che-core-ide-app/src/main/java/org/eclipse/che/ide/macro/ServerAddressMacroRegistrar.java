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

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.BaseMacro;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;

/**
 * For every server in WsAgent's machine registers a {@link Macro} that returns server's external
 * address in form <b>hostname:port</b>.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ServerAddressMacroRegistrar implements WsAgentStateHandler {

  public static final String MACRO_NAME_TEMPLATE = "${server.port.%}";

  private final MacroRegistry macroRegistry;
  private final AppContext appContext;

  private Set<Macro> macros;

  @Inject
  public ServerAddressMacroRegistrar(
      EventBus eventBus, MacroRegistry macroRegistry, AppContext appContext) {
    this.macroRegistry = macroRegistry;
    this.appContext = appContext;

    eventBus.addHandler(WsAgentStateEvent.TYPE, this);

    registerMacros();
  }

  private void registerMacros() {
    Machine devMachine = appContext.getDevMachine();
    if (devMachine != null) {
      macros = getMacros(devMachine);
      macroRegistry.register(macros);
    }
  }

  private Set<Macro> getMacros(Machine machine) {
    Set<Macro> macros = Sets.newHashSet();
    for (Map.Entry<String, ? extends Server> entry : machine.getRuntime().getServers().entrySet()) {
      macros.add(new ServerAddressMacro(entry.getKey(), entry.getValue().getAddress()));

      if (entry.getKey().endsWith("/tcp")) {
        macros.add(
            new ServerAddressMacro(
                entry.getKey().substring(0, entry.getKey().length() - 4),
                entry.getValue().getAddress()));
      }
    }

    return macros;
  }

  @Override
  public void onWsAgentStarted(WsAgentStateEvent event) {
    registerMacros();
  }

  @Override
  public void onWsAgentStopped(WsAgentStateEvent event) {
    for (Macro provider : macros) {
      macroRegistry.unregister(provider);
    }

    macros.clear();
  }

  private class ServerAddressMacro extends BaseMacro {
    ServerAddressMacro(String internalPort, String externalAddress) {
      super(
          MACRO_NAME_TEMPLATE.replaceAll("%", internalPort),
          externalAddress,
          "Returns external address of the server running on port " + internalPort);
    }
  }
}
