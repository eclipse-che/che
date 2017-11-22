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

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.BaseMacro;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

/**
 * For every server in dev-machine registers a {@link Macro} that returns server's URL.
 *
 * <p>Macro name: <code>${server.server_reference}</code>.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ServerAddressMacroRegistrar {

  private static final String MACRO_NAME_TEMPLATE = "${server.%}";

  private final Provider<MacroRegistry> macroRegistryProvider;
  private final AppContext appContext;
  private final WsAgentServerUtil wsAgentServerUtil;

  private Set<Macro> macros;

  @Inject
  public ServerAddressMacroRegistrar(
      EventBus eventBus,
      Provider<MacroRegistry> macroRegistryProvider,
      AppContext appContext,
      WsAgentServerUtil wsAgentServerUtil) {
    this.macroRegistryProvider = macroRegistryProvider;
    this.appContext = appContext;
    this.wsAgentServerUtil = wsAgentServerUtil;

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        e -> {
          if (appContext.getWorkspace().getStatus() == RUNNING) {
            registerMacros();
          }
        });

    eventBus.addHandler(WorkspaceRunningEvent.TYPE, e -> registerMacros());

    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          macros.forEach(macro -> macroRegistryProvider.get().unregister(macro));
          macros.clear();
        });
  }

  private void registerMacros() {
    final Optional<MachineImpl> devMachine = wsAgentServerUtil.getWsAgentServerMachine();

    if (devMachine.isPresent()) {
      macros = getMacros(devMachine.get());
      macroRegistryProvider.get().register(macros);
    }
  }

  private Set<Macro> getMacros(Machine machine) {
    Set<Macro> macros = new HashSet<>();

    for (Map.Entry<String, ? extends Server> entry : machine.getServers().entrySet()) {
      macros.add(new ServerAddressMacro(entry.getKey(), entry.getValue().getUrl()));
    }

    return macros;
  }

  private class ServerAddressMacro extends BaseMacro {
    ServerAddressMacro(String reference, String url) {
      super(
          MACRO_NAME_TEMPLATE.replaceAll("%", reference),
          url,
          "Returns address of the " + reference + " server");
    }
  }
}
