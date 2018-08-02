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
package org.eclipse.che.plugin.debugger.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Registry of {@link DebugConfigurationType}s.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugConfigurationTypeRegistry {

  private final Map<String, DebugConfigurationType> types;

  public DebugConfigurationTypeRegistry() {
    this.types = new HashMap<>();
  }

  @Inject(optional = true)
  private void register(Set<DebugConfigurationType> debugConfigurationTypes) {
    for (DebugConfigurationType type : debugConfigurationTypes) {
      final String id = type.getId();
      if (this.types.containsKey(id)) {
        Log.warn(
            DebugConfigurationTypeRegistry.class,
            "Debug configuration type with ID " + id + " is already registered.");
      } else {
        this.types.put(id, type);
      }
    }
  }

  /**
   * Returns {@link DebugConfigurationType} by the given ID or {@code null} if none.
   *
   * @param id the ID of the debug configuration type
   * @return {@link DebugConfigurationType} or {@code null}
   */
  @Nullable
  public DebugConfigurationType getConfigurationTypeById(String id) {
    return types.get(id);
  }

  /** Returns all registered debug configuration types. */
  @NotNull
  public Collection<DebugConfigurationType> getTypes() {
    return types.values();
  }
}
