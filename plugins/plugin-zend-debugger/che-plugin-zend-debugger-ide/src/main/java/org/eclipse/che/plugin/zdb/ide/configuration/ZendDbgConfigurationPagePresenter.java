/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide.configuration;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;

/**
 * Page allows to edit Zend debugger configuration.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgConfigurationPagePresenter
    implements ZendDbgConfigurationPageView.ActionDelegate,
        DebugConfigurationPage<DebugConfiguration> {

  private final ZendDbgConfigurationPageView view;
  private DebugConfiguration editedConfiguration;
  private String originClientHostIp;
  private int originDebugPort;
  private boolean originUseSsslEncryption;
  private boolean originBreakAtFirstLine;
  private DirtyStateListener listener;
  private Provider<DebugConfigurationsManager> debugConfigurationsManagerProvider;

  @Inject
  public ZendDbgConfigurationPagePresenter(
      ZendDbgConfigurationPageView view,
      Provider<DebugConfigurationsManager> debugConfigurationsManagerProvider) {
    this.view = view;
    this.debugConfigurationsManagerProvider = debugConfigurationsManagerProvider;
    view.setDelegate(this);
  }

  @Override
  public void resetFrom(DebugConfiguration configuration) {
    setConfigurationDefaults(configuration);
    editedConfiguration = configuration;

    Map<String, String> props = configuration.getConnectionProperties();
    originBreakAtFirstLine =
        Boolean.valueOf(props.get(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE));
    originClientHostIp = props.get(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP);
    originDebugPort = Integer.valueOf(props.get(ZendDbgConfigurationType.ATTR_DEBUG_PORT));
    originUseSsslEncryption =
        Boolean.valueOf(props.get(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION));
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    Map<String, String> props = editedConfiguration.getConnectionProperties();
    view.setBreakAtFirstLine(
        Boolean.valueOf(props.get(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE)));
    view.setClientHostIP(props.get(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP));
    view.setDebugPort(Integer.valueOf(props.get(ZendDbgConfigurationType.ATTR_DEBUG_PORT)));
    view.setUseSslEncryption(
        Boolean.valueOf(props.get(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION)));
  }

  @Override
  public boolean isDirty() {
    Map<String, String> props = editedConfiguration.getConnectionProperties();

    return originBreakAtFirstLine
            != Boolean.valueOf(props.get(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE))
        || !originClientHostIp.equals(props.get(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP))
        || originDebugPort != Integer.valueOf(props.get(ZendDbgConfigurationType.ATTR_DEBUG_PORT))
        || originUseSsslEncryption
            != Boolean.valueOf(props.get(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION));
  }

  @Override
  public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void onBreakAtFirstLineChanged(boolean value) {
    Map<String, String> props = editedConfiguration.getConnectionProperties();
    props.put(
        ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE,
        String.valueOf(view.getBreakAtFirstLine()));
    listener.onDirtyStateChanged();
  }

  @Override
  public void onClientHostIPChanged() {
    Map<String, String> props = editedConfiguration.getConnectionProperties();
    props.put(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP, view.getClientHostIP());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onDebugPortChanged() {
    Map<String, String> props = editedConfiguration.getConnectionProperties();
    props.put(ZendDbgConfigurationType.ATTR_DEBUG_PORT, String.valueOf(view.getDebugPort()));
    listener.onDirtyStateChanged();
  }

  @Override
  public void onUseSslEncryptionChanged(boolean value) {
    Map<String, String> props = editedConfiguration.getConnectionProperties();
    props.put(
        ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION,
        String.valueOf(view.getUseSslEncryption()));
    listener.onDirtyStateChanged();
  }

  private void setConfigurationDefaults(DebugConfiguration configuration) {
    if (!configuration.getConnectionProperties().isEmpty()) {
      return;
    }
    DebugConfigurationsManager debugConfigurationsManager =
        debugConfigurationsManagerProvider.get();
    debugConfigurationsManager.removeConfiguration(configuration);
    ZendDbgConfigurationType.setDefaults(configuration);
    debugConfigurationsManager.createConfiguration(
        configuration.getType().getId(),
        configuration.getName(),
        configuration.getHost(),
        configuration.getPort(),
        configuration.getConnectionProperties());
  }
}
