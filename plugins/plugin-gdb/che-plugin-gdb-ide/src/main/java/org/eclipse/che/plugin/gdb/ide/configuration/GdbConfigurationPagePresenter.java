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
package org.eclipse.che.plugin.gdb.ide.configuration;

import static java.util.Collections.emptyList;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.machine.MachineEntityImpl;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;

/**
 * Page allows to edit GDB debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GdbConfigurationPagePresenter
    implements GdbConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

  public static final String BIN_PATH_CONNECTION_PROPERTY = "BINARY";
  public static final String DEFAULT_EXECUTABLE_TARGET_NAME = "a.out";

  private final GdbConfigurationPageView view;
  private final AppContext appContext;
  private final CurrentProjectPathMacro currentProjectPathMacro;

  private DebugConfiguration editedConfiguration;
  private String originHost;
  private int originPort;
  private String originBinaryPath;
  private DirtyStateListener listener;

  @Inject
  public GdbConfigurationPagePresenter(
      GdbConfigurationPageView view,
      AppContext appContext,
      CurrentProjectPathMacro currentProjectPathMacro) {
    this.view = view;
    this.appContext = appContext;
    this.currentProjectPathMacro = currentProjectPathMacro;

    view.setDelegate(this);
  }

  @Override
  public void resetFrom(DebugConfiguration configuration) {
    editedConfiguration = configuration;

    originHost = configuration.getHost();
    originPort = configuration.getPort();
    originBinaryPath = getBinaryPath(configuration);

    if (originBinaryPath == null) {
      String defaultBinaryPath = getDefaultBinaryPath();
      editedConfiguration
          .getConnectionProperties()
          .put(BIN_PATH_CONNECTION_PROPERTY, defaultBinaryPath);
      originBinaryPath = defaultBinaryPath;
    }
  }

  private String getBinaryPath(DebugConfiguration debugConfiguration) {
    Map<String, String> connectionProperties = debugConfiguration.getConnectionProperties();
    return connectionProperties.get(BIN_PATH_CONNECTION_PROPERTY);
  }

  private String getDefaultBinaryPath() {
    return currentProjectPathMacro.getName() + "/" + DEFAULT_EXECUTABLE_TARGET_NAME;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setHost(editedConfiguration.getHost());
    view.setPort(editedConfiguration.getPort());
    view.setBinaryPath(getBinaryPath(editedConfiguration));

    boolean devHost =
        "localhost".equals(editedConfiguration.getHost()) && editedConfiguration.getPort() <= 0;
    view.setDevHost(devHost);
    view.setPortEnableState(!devHost);
    view.setHostEnableState(!devHost);

    List<Machine> machines = getMachines();
    if (!machines.isEmpty()) {
      setHosts(machines);
    }
  }

  private void setHosts(List<Machine> machines) {
    Map<String, String> hosts = new HashMap<>();
    for (Machine machine : machines) {
      String host = machine.getRuntime().getProperties().get("network.ipAddress");
      if (host == null) {
        continue;
      }

      String description = host + " (" + machine.getConfig().getName() + ")";
      hosts.put(host, description);
    }

    view.setHostsList(hosts);
  }

  private List<Machine> getMachines() {
    Workspace workspace = appContext.getWorkspace();
    if (workspace == null || workspace.getRuntime() == null) {
      return emptyList();
    }

    List<? extends Machine> runtimeMachines = workspace.getRuntime().getMachines();
    List<Machine> machines = new ArrayList<>(runtimeMachines.size());
    for (Machine currentMachine : runtimeMachines) {
      if (currentMachine instanceof MachineDto) {
        Machine machine = new MachineEntityImpl(currentMachine);
        machines.add(machine);
      }
    }
    return machines;
  }

  @Override
  public boolean isDirty() {
    return !originHost.equals(editedConfiguration.getHost())
        || originPort != editedConfiguration.getPort()
        || !originBinaryPath.equals(getBinaryPath(editedConfiguration));
  }

  @Override
  public void setDirtyStateListener(DirtyStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void onHostChanged() {
    editedConfiguration.setHost(view.getHost());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onPortChanged() {
    editedConfiguration.setPort(view.getPort());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onBinaryPathChanged() {
    final Map<String, String> connectionProperties = editedConfiguration.getConnectionProperties();
    connectionProperties.put(BIN_PATH_CONNECTION_PROPERTY, view.getBinaryPath());

    editedConfiguration.setConnectionProperties(connectionProperties);
    listener.onDirtyStateChanged();
  }

  @Override
  public void onDevHostChanged(boolean value) {
    view.setHostEnableState(!value);
    view.setPortEnableState(!value);
    if (value) {
      editedConfiguration.setHost("localhost");
      view.setHost(editedConfiguration.getHost());

      editedConfiguration.setPort(0);
      view.setPort(0);

      listener.onDirtyStateChanged();
    }
  }
}
