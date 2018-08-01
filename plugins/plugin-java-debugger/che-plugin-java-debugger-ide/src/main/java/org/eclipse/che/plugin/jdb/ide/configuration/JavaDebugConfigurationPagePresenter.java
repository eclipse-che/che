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
package org.eclipse.che.plugin.jdb.ide.configuration;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.util.Pair;

/**
 * Page allows to edit Java debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class JavaDebugConfigurationPagePresenter
    implements JavaDebugConfigurationPageView.ActionDelegate,
        DebugConfigurationPage<DebugConfiguration> {

  private final JavaDebugConfigurationPageView view;
  private final WsAgentServerUtil wsAgentServerUtil;

  private DebugConfiguration editedConfiguration;
  private String originHost;
  private int originPort;
  private DirtyStateListener listener;

  @Inject
  public JavaDebugConfigurationPagePresenter(
      JavaDebugConfigurationPageView view, WsAgentServerUtil wsAgentServerUtil) {
    this.view = view;
    this.wsAgentServerUtil = wsAgentServerUtil;

    view.setDelegate(this);
  }

  @Override
  public void resetFrom(DebugConfiguration configuration) {
    editedConfiguration = configuration;

    originHost = configuration.getHost();
    originPort = configuration.getPort();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    final String host = editedConfiguration.getHost();

    view.setHost(host);
    view.setPort(editedConfiguration.getPort());
    view.setDevHost("localhost".equals(host));

    setPortsList();
  }

  private void setPortsList() {
    wsAgentServerUtil
        .getWsAgentServerMachine()
        .ifPresent(machine -> view.setPortsList(extractPortsList(machine)));
  }

  /** Extracts list of ports available for connecting to the remote debugger. */
  private List<Pair<String, String>> extractPortsList(final MachineImpl machine) {
    List<Pair<String, String>> ports = new ArrayList<>();
    if (machine == null) {
      return ports;
    }

    Map<String, ? extends Server> servers = machine.getServers();
    for (Map.Entry<String, ? extends Server> entry : servers.entrySet()) {
      String port = entry.getKey();
      if (port.endsWith("/tcp")) {
        String portWithoutTcp = port.substring(0, port.length() - 4);
        String description = portWithoutTcp + " (" + entry.getValue().getUrl() + ")";
        Pair<String, String> pair = new Pair<>(description, portWithoutTcp);

        ports.add(pair);
      }
    }

    return ports;
  }

  @Override
  public boolean isDirty() {
    return !originHost.equals(editedConfiguration.getHost())
        || originPort != editedConfiguration.getPort();
  }

  @Override
  public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
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
  public void onDevHostChanged(boolean value) {
    view.setHostEnableState(!value);
    if (value) {
      editedConfiguration.setHost("localhost");
      view.setHost(editedConfiguration.getHost());
      listener.onDirtyStateChanged();
    }
  }
}
