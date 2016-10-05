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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The implementation of {@link ZendDebugConfigurationPageView}.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugConfigurationPageViewImpl implements ZendDebugConfigurationPageView {

	private static final ZendDebugConfigurationPageViewImplUiBinder UI_BINDER = GWT
			.create(ZendDebugConfigurationPageViewImplUiBinder.class);

	private final FlowPanel rootElement;

	@UiField
	TextBox clientHostIP;
	@UiField
	TextBox debugPort;
	@UiField
	TextBox broadcastPort;
	@UiField
	CheckBox useSslEncryption;

	private ActionDelegate delegate;

	public ZendDebugConfigurationPageViewImpl() {
		rootElement = UI_BINDER.createAndBindUi(this);

		useSslEncryption.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				delegate.onUseSslEncryptionChanged(event.getValue());
			}
		});

		updateDialog();
	}

	@Override
	public void setDelegate(ActionDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public Widget asWidget() {
		return rootElement;
	}

	@Override
	public String getClientHostIP() {
		return clientHostIP.getValue();
	}

	@Override
	public void setClientHostIP(String host) {
		this.clientHostIP.setValue(host);
	}

	@Override
	public int getDebugPort() {
		String port = this.debugPort.getValue().trim();
		if (port.isEmpty()) {
			return 0;
		}

		try {
			return Integer.valueOf(port);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public void setDebugPort(int port) {
		this.debugPort.setValue(port <= 0 ? "" : String.valueOf(port));
	}
	
	@Override
	public int getBroadcastPort() {
		String port = this.broadcastPort.getValue().trim();
		if (port.isEmpty()) {
			return 0;
		}

		try {
			return Integer.valueOf(port);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public void setBroadcastPort(int port) {
		this.broadcastPort.setValue(port <= 0 ? "" : String.valueOf(port));
	}

	@Override
	public boolean getUseSslEncryption() {
		return this.useSslEncryption.getValue();
	}

	@Override
	public void setUseSslEncryption(boolean value) {
		this.useSslEncryption.setValue(value);
	}

	private void updateDialog() {
		clientHostIP.setFocus(true);
	}

	@UiHandler({ "clientHostIP" })
	void onClientHostIPChanged(KeyUpEvent event) {
		delegate.onClientHostIPChanged();
	}

	@UiHandler({ "debugPort" })
	void onDebugPortChanged(KeyUpEvent event) {
		delegate.onDebugPortChanged();
	}

	@UiHandler({ "broadcastPort" })
	void onBroadcastPortChanged(KeyUpEvent event) {
		delegate.onBroadcastPortChanged();
	}

	interface ZendDebugConfigurationPageViewImplUiBinder
			extends UiBinder<FlowPanel, ZendDebugConfigurationPageViewImpl> {
	}

}
