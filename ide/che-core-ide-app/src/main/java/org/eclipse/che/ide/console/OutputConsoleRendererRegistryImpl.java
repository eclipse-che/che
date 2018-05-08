/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.console.OutputConsoleRenderer;
import org.eclipse.che.ide.api.console.OutputConsoleRendererRegistry;

public class OutputConsoleRendererRegistryImpl implements OutputConsoleRendererRegistry {
  private final Map<String, Set<OutputConsoleRenderer>> registry; // type-2-renderers
  private final Map<String, Set<String>> commandRegistry; // type-2-commands

  /** Constructs the Output Renders registry object */
  public OutputConsoleRendererRegistryImpl() {
    registry = new HashMap<>();
    commandRegistry = new HashMap<>();
  }

  @Override
  public void register(String type, OutputConsoleRenderer renderer) {
    if (!registry.containsKey(type)) {
      registry.put(type, new HashSet<OutputConsoleRenderer>());
    }

    registry.get(type).add(renderer);
  }

  @Override
  public void registerCommandDefault(String commandType, String type) {
    if (!commandRegistry.containsKey(commandType)) {
      commandRegistry.put(commandType, new HashSet<String>());
    }
    commandRegistry.get(commandType).add(type);
  }

  @Override
  public Set<OutputConsoleRenderer> getOutputRenderers(String type) {
    return registry.get(type);
  }

  @Override
  public Set<String> getAllOutputRendererTypes() {
    return registry.keySet();
  }

  @Override
  public Set<OutputConsoleRenderer> getAllOutputRenderers() {
    Set<OutputConsoleRenderer> result = new HashSet<OutputConsoleRenderer>();
    registry.values().forEach(renderers -> result.addAll(renderers));
    return result;
  }

  @Override
  public Set<String> getCommandDefaultOutputRenderers(String commandType) {
    return commandRegistry.getOrDefault(commandType, new HashSet<>());
  }
}
