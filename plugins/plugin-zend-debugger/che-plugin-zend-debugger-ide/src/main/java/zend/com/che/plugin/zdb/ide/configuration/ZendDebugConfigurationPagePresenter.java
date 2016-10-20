/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide.configuration;

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
public class ZendDebugConfigurationPagePresenter
		implements ZendDebugConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

	private final ZendDebugConfigurationPageView view;

	private DebugConfiguration editedConfiguration;
	private String originClientHostIp;
	private int originDebugPort;
	private boolean originUseSsslEncryption;
	private boolean originBreakAtFirstLine;
	private DirtyStateListener listener;
	private Provider<DebugConfigurationsManager> debugConfigurationsManagerProvider;

	@Inject
	public ZendDebugConfigurationPagePresenter(ZendDebugConfigurationPageView view,
			Provider<DebugConfigurationsManager> debugConfigurationsManagerProvider) {
		this.view = view;
		this.debugConfigurationsManagerProvider = debugConfigurationsManagerProvider;
		view.setDelegate(this);
	}

	@Override
	public void resetFrom(DebugConfiguration configuration) {
		setConfigurationDefaults(configuration);
		editedConfiguration = configuration;
		originClientHostIp = configuration.getConnectionProperties()
				.get(ZendDebugConfigurationType.ATTR_CLIENT_HOST_IP);
		originDebugPort = Integer
				.valueOf(configuration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_DEBUG_PORT));
		originBreakAtFirstLine = Boolean.valueOf(
				configuration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_BREAK_AT_FIRST_LINE));
		originUseSsslEncryption = Boolean.valueOf(
				configuration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_USE_SSL_ENCRYPTION));
	}

	@Override
	public void go(AcceptsOneWidget container) {
		container.setWidget(view);
		view.setClientHostIP(
				editedConfiguration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_CLIENT_HOST_IP));
		view.setDebugPort(Integer.valueOf(
				editedConfiguration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_DEBUG_PORT)));
		view.setBreakAtFirstLine(Boolean.valueOf(
				editedConfiguration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_BREAK_AT_FIRST_LINE)));
		view.setUseSslEncryption(Boolean.valueOf(
				editedConfiguration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_USE_SSL_ENCRYPTION)));
	}

	@Override
	public boolean isDirty() {
		return !originClientHostIp.equals(
				editedConfiguration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_CLIENT_HOST_IP))
				|| originDebugPort != Integer.valueOf(
						editedConfiguration.getConnectionProperties().get(ZendDebugConfigurationType.ATTR_DEBUG_PORT))
				|| originBreakAtFirstLine != Boolean.valueOf(editedConfiguration.getConnectionProperties()
						.get(ZendDebugConfigurationType.ATTR_BREAK_AT_FIRST_LINE))
				|| originUseSsslEncryption != Boolean.valueOf(editedConfiguration.getConnectionProperties()
						.get(ZendDebugConfigurationType.ATTR_USE_SSL_ENCRYPTION));
	}

	@Override
	public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClientHostIPChanged() {
		editedConfiguration.getConnectionProperties().put(ZendDebugConfigurationType.ATTR_CLIENT_HOST_IP,
				view.getClientHostIP());
		listener.onDirtyStateChanged();
	}

	@Override
	public void onDebugPortChanged() {
		editedConfiguration.getConnectionProperties().put(ZendDebugConfigurationType.ATTR_DEBUG_PORT,
				String.valueOf(view.getDebugPort()));
		listener.onDirtyStateChanged();
	}

	@Override
	public void onBreakAtFirstLineChanged(boolean value) {
		editedConfiguration.getConnectionProperties().put(ZendDebugConfigurationType.ATTR_BREAK_AT_FIRST_LINE,
				String.valueOf(view.getBreakAtFirstLine()));
		listener.onDirtyStateChanged();
	}

	@Override
	public void onUseSslEncryptionChanged(boolean value) {
		editedConfiguration.getConnectionProperties().put(ZendDebugConfigurationType.ATTR_USE_SSL_ENCRYPTION,
				String.valueOf(view.getUseSslEncryption()));
		listener.onDirtyStateChanged();
	}

	private void setConfigurationDefaults(DebugConfiguration configuration) {
		if (!configuration.getConnectionProperties().isEmpty()) {
			return;
		}
		DebugConfigurationsManager debugConfigurationsManager = debugConfigurationsManagerProvider.get();
		debugConfigurationsManager.removeConfiguration(configuration);
		ZendDebugConfigurationType.setDefaults(configuration);
		debugConfigurationsManager.createConfiguration(configuration.getType().getId(), configuration.getName(),
				configuration.getHost(), configuration.getPort(), configuration.getConnectionProperties());
	}

}
