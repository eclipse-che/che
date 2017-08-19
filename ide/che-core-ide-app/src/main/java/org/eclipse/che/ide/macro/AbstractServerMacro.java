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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;

/**
 * Base macro which belongs to the current server configuration. Provides easy access to the
 * developer machine to allow fetch necessary information to use in custom commands, preview urls,
 * etc.
 *
 * @author Vlad Zhukovskyi
 * @see MacroRegistry
 * @see Macro
 * @see ServerHostNameMacro
 * @see ServerMacro
 * @see ServerPortMacro
 * @see ServerProtocolMacro
 * @since 4.7.0
 */
@Beta
public abstract class AbstractServerMacro implements WsAgentStateHandler {

  private final MacroRegistry macroRegistry;
  private final AppContext appContext;

  public AbstractServerMacro(
      MacroRegistry macroRegistry, EventBus eventBus, AppContext appContext) {
    this.macroRegistry = macroRegistry;
    this.appContext = appContext;

    eventBus.addHandler(WsAgentStateEvent.TYPE, this);
  }

  /**
   * Register macro providers which returns the implementation.
   *
   * @see AbstractServerMacro#getMacros(DevMachine)
   * @since 4.7.0
   */
  private void registerMacros() {
    final DevMachine devMachine = appContext.getDevMachine();

    if (devMachine == null) {
      return;
    }

    final Set<Macro> macros = getMacros(devMachine);
    checkNotNull(macros);

    if (macros.isEmpty()) {
      return;
    }

    macroRegistry.register(macros);
  }

  /**
   * Unregister macro providers which the implementation returns.
   *
   * @see AbstractServerMacro#getMacros(DevMachine)
   * @since 4.7.0
   */
  private void unregisterMacros() {
    final DevMachine devMachine = appContext.getDevMachine();

    if (devMachine == null) {
      return;
    }

    for (Macro provider : getMacros(devMachine)) {
      macroRegistry.unregister(provider);
    }
  }

  /**
   * Returns the macros which implementation provides based on the information from the developer
   * machine.
   *
   * @param devMachine current developer machine
   * @return set of unique macro providers
   * @see DevMachine
   * @see Macro
   * @since 4.7.0
   */
  public abstract Set<Macro> getMacros(DevMachine devMachine);

  /** {@inheritDoc} */
  @Override
  public void onWsAgentStarted(WsAgentStateEvent event) {
    registerMacros();
  }

  /** {@inheritDoc} */
  @Override
  public void onWsAgentStopped(WsAgentStateEvent event) {
    unregisterMacros();
  }
}
