/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.debug;

import com.google.common.base.Optional;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Facade for debug configuration related operations.
 *
 * <p>Also holds the current debug configuration. Current means the configuration which should be
 * used for connecting to the debugger.
 *
 * @author Artem Zatsarynnyi
 */
public interface DebugConfigurationsManager {

  /** Returns current debug configuration. */
  Optional<DebugConfiguration> getCurrentDebugConfiguration();

  /** Set current debug configuration. */
  void setCurrentDebugConfiguration(@Nullable DebugConfiguration debugConfiguration);

  /** Returns all debug configurations. */
  List<DebugConfiguration> getConfigurations();

  /**
   * Creates new configuration with the given parameters.
   *
   * @return created {@link DebugConfiguration}
   */
  DebugConfiguration createConfiguration(
      String typeId, String name, String host, int port, Map<String, String> connectionProperties);

  /** Remove the given debug configuration. */
  void removeConfiguration(DebugConfiguration configuration);

  /** Add listener to be notified when some debug configuration has been changed. */
  void addConfigurationsChangedListener(ConfigurationChangedListener listener);

  /** Remove the given listener. */
  void removeConfigurationsChangedListener(ConfigurationChangedListener listener);

  /**
   * Apply configuration. Establish connection with configured debugger.
   *
   * @param debugConfiguration the debug configuration to use
   */
  void apply(DebugConfiguration debugConfiguration);

  /** Listener that will be called when debug configuration has been changed. */
  interface ConfigurationChangedListener {
    void onConfigurationAdded(DebugConfiguration configuration);

    void onConfigurationRemoved(DebugConfiguration configuration);
  }
}
