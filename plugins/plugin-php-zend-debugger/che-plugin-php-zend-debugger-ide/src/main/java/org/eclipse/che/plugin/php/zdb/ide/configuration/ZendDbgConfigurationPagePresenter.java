/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.php.zdb.ide.configuration;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;

import javax.validation.constraints.NotNull;

/**
 * Page allows to edit Zend debugger configuration.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgConfigurationPagePresenter
		implements ZendDbgConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

	private final ZendDbgConfigurationPageView view;
	private DebugConfiguration editedConfiguration;
	private String originClientHostIp;
	private int originDebugPort;
	private boolean originUseSsslEncryption;
	private boolean originBreakAtFirstLine;
	private DirtyStateListener listener;
	private Provider<DebugConfigurationsManager> debugConfigurationsManagerProvider;

	@Inject
	public ZendDbgConfigurationPagePresenter(ZendDbgConfigurationPageView view,
			Provider<DebugConfigurationsManager> debugConfigurationsManagerProvider) {
		this.view = view;
		this.debugConfigurationsManagerProvider = debugConfigurationsManagerProvider;
		view.setDelegate(this);
	}

	@Override
	public void resetFrom(DebugConfiguration configuration) {
		setConfigurationDefaults(configuration);
		editedConfiguration = configuration;
		originBreakAtFirstLine = Boolean.valueOf(configuration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE));
		originClientHostIp = configuration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP);
		originDebugPort = Integer.valueOf(configuration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_DEBUG_PORT));
		originUseSsslEncryption = Boolean.valueOf(configuration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION));
	}

	@Override
	public void go(AcceptsOneWidget container) {
		container.setWidget(view);
		view.setBreakAtFirstLine(Boolean.valueOf(editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE)));
		view.setClientHostIP(editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP));
		view.setDebugPort(Integer.valueOf(editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_DEBUG_PORT)));
		view.setUseSslEncryption(Boolean.valueOf(editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION)));
	}

	@Override
	public boolean isDirty() {
		return originBreakAtFirstLine != Boolean.valueOf(
				editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE))
				|| !originClientHostIp.equals(
						editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP))
				|| originDebugPort != Integer.valueOf(
						editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_DEBUG_PORT))
				|| originUseSsslEncryption != Boolean.valueOf(
						editedConfiguration.getConnectionProperties().get(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION));
	}

	@Override
	public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
		this.listener = listener;
	}

	@Override
	public void onBreakAtFirstLineChanged(boolean value) {
		editedConfiguration.getConnectionProperties().put(ZendDbgConfigurationType.ATTR_BREAK_AT_FIRST_LINE,
				String.valueOf(view.getBreakAtFirstLine()));
		listener.onDirtyStateChanged();
	}

	@Override
	public void onClientHostIPChanged() {
		editedConfiguration.getConnectionProperties().put(ZendDbgConfigurationType.ATTR_CLIENT_HOST_IP,
				view.getClientHostIP());
		listener.onDirtyStateChanged();
	}

	@Override
	public void onDebugPortChanged() {
		editedConfiguration.getConnectionProperties().put(ZendDbgConfigurationType.ATTR_DEBUG_PORT,
				String.valueOf(view.getDebugPort()));
		listener.onDirtyStateChanged();
	}

	@Override
	public void onUseSslEncryptionChanged(boolean value) {
		editedConfiguration.getConnectionProperties().put(ZendDbgConfigurationType.ATTR_USE_SSL_ENCRYPTION,
				String.valueOf(view.getUseSslEncryption()));
		listener.onDirtyStateChanged();
	}

	private void setConfigurationDefaults(DebugConfiguration configuration) {
		if (!configuration.getConnectionProperties().isEmpty()) {
			return;
		}
		DebugConfigurationsManager debugConfigurationsManager = debugConfigurationsManagerProvider.get();
		debugConfigurationsManager.removeConfiguration(configuration);
		ZendDbgConfigurationType.setDefaults(configuration);
		debugConfigurationsManager.createConfiguration(configuration.getType().getId(), configuration.getName(),
				configuration.getHost(), configuration.getPort(), configuration.getConnectionProperties());
	}

}
