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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import static org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebugger.ConnectionProperties.SCRIPT;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;

/**
 * Page allows to edit NodeJs debug configuration.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class NodeJsDebuggerConfigurationPagePresenter
    implements NodeJsDebuggerConfigurationPageView.ActionDelegate,
        DebugConfigurationPage<DebugConfiguration> {

  private static final String DEFAULT_SCRIPT_NAME = "app.js";

  private final NodeJsDebuggerConfigurationPageView view;
  private final CurrentProjectPathMacro currentProjectPathMacro;

  private DebugConfiguration editedConfiguration;
  private String originScriptPath;
  private DirtyStateListener listener;

  @Inject
  public NodeJsDebuggerConfigurationPagePresenter(
      NodeJsDebuggerConfigurationPageView view, CurrentProjectPathMacro currentProjectPathMacro) {
    this.view = view;
    this.currentProjectPathMacro = currentProjectPathMacro;

    view.setDelegate(this);
  }

  @Override
  public void resetFrom(DebugConfiguration configuration) {
    editedConfiguration = configuration;
    originScriptPath = getScriptPath(configuration);

    if (originScriptPath == null) {
      String defaultBinaryPath = getDefaultBinaryPath();
      editedConfiguration.getConnectionProperties().put(SCRIPT.toString(), defaultBinaryPath);
      originScriptPath = defaultBinaryPath;
    }
  }

  private String getScriptPath(DebugConfiguration debugConfiguration) {
    Map<String, String> connectionProperties = debugConfiguration.getConnectionProperties();
    return connectionProperties.get(SCRIPT.toString());
  }

  private String getDefaultBinaryPath() {
    return currentProjectPathMacro.getName() + "/" + DEFAULT_SCRIPT_NAME;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    view.setScriptPath(getScriptPath(editedConfiguration));
  }

  @Override
  public boolean isDirty() {
    return !originScriptPath.equals(getScriptPath(editedConfiguration));
  }

  @Override
  public void setDirtyStateListener(DirtyStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void onScriptPathChanged() {
    final Map<String, String> connectionProperties = editedConfiguration.getConnectionProperties();
    connectionProperties.put(SCRIPT.toString(), view.getScriptPath());

    editedConfiguration.setConnectionProperties(connectionProperties);
    listener.onDirtyStateChanged();
  }
}
