/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.targets.categories.ssh;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.TextBox;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.ui.window.Window.Resources;

/**
 * The implementation of {@link SshView}.
 *
 * @author Oleksii Orel
 */
@Singleton
public class SshViewImpl implements SshView {

    private static final SshViewImplUiBinder UI_BINDER = GWT.create(SshViewImplUiBinder.class);

    private final FlowPanel rootElement;
    private final Resources windowResources;

    private ActionDelegate delegate;


    @UiField
    TextBox targetName;

    @UiField
    TextBox host;

    @UiField
    TextBox port;

    @UiField
    TextBox userName;

    @UiField
    PasswordTextBox password;

    @UiField
    FlowPanel footer;

    private Button saveButton;
    private Button cancelButton;
    private Button connectButton;

    @Inject
    public SshViewImpl(org.eclipse.che.ide.Resources resources,
                       org.eclipse.che.ide.ui.window.Window.Resources windowResources,
                       CoreLocalizationConstant coreLocale) {

        this.windowResources = windowResources;
        this.rootElement = UI_BINDER.createAndBindUi(this);


        saveButton = createButton(coreLocale.save(), "targets.button.save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSaveClicked();
            }
        });
        saveButton.addStyleName(this.windowResources.windowCss().primaryButton());
        footer.add(saveButton);

        cancelButton = createButton(coreLocale.cancel(), "targets.button.cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        footer.add(cancelButton);

        connectButton = createButton("Connect", "targets.button.connect", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onConnectClicked();
            }
        });
        connectButton.addStyleName(this.windowResources.windowCss().primaryButton());
        connectButton.addStyleName(resources.Css().buttonLoader());
        footer.add(connectButton);

        targetName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onTargetNameChanged(targetName.getValue());
            }
        });

        host.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onHostChanged(host.getValue());
            }
        });

        port.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onPortChanged(port.getValue());
            }
        });

        userName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onUserNameChanged(userName.getValue());
            }
        });

        password.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                delegate.onPasswordChanged(password.getValue());
            }
        });

        rootElement.setVisible(true);
    }

    private Button createButton(String title, String debugId, ClickHandler clickHandler) {
        Button button = new Button();
        button.setText(title);
        button.ensureDebugId(debugId);
        button.getElement().setId(debugId);
        button.addStyleName(this.windowResources.windowCss().button());
        button.addClickHandler(clickHandler);
        //set default tab index
        button.setTabIndex(0);
        return button;
    }


    @Override
    public Widget asWidget() {
        return rootElement;
    }


    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTargetName(String targetName) {
        this.targetName.setValue(targetName);
    }

    @Override
    public String getTargetName() {
        return targetName.getValue();
    }

    @Override
    public void markTargetNameInvalid() {
        targetName.markInvalid();
    }

    @Override
    public void unmarkTargetName() {
        targetName.unmark();
    }

    @Override
    public void markHostInvalid() {
        host.markInvalid();
    }

    @Override
    public void unmarkHost() {
        host.unmark();
    }

    @Override
    public void markPortInvalid() {
        port.markInvalid();
    }

    @Override
    public void unmarkPort() {
        port.unmark();
    }

    @Override
    public void setHost(String host) {
        this.host.setValue(host);
    }

    @Override
    public String getHost() {
        return host.getValue();
    }

    @Override
    public void setPort(String port) {
        this.port.setValue(port);
    }

    @Override
    public String getPort() {
        return port.getValue();
    }

    @Override
    public void setUserName(String userName) {
        this.userName.setValue(userName);
    }

    @Override
    public String getUserName() {
        return userName.getValue();
    }

    @Override
    public void setPassword(String password) {
        this.password.setValue(password);
    }

    @Override
    public String getPassword() {
        return password.getValue();
    }

    @Override
    public void enableSaveButton(boolean enable) {
        saveButton.setEnabled(enable);
    }

    @Override
    public void enableCancelButton(boolean enable) {
        cancelButton.setEnabled(enable);
    }

    @Override
    public boolean restoreTargetFields(SshMachineTarget target) {
        if (target == null) {
            return false;
        }

        final RecipeDescriptor targetRecipe = target.getRecipe();
        if (targetRecipe == null) {
            return false;
        }

        try {
            final JSONObject json = JSONParser.parseStrict(targetRecipe.getScript()).isObject();

            String name = targetRecipe.getName();
            target.setName(name);

            if (json.get("host") != null) {
                String host = json.get("host").isString().stringValue();
                target.setHost(host);
            }

            if (json.get("port") != null) {
                String port = json.get("port").isString().stringValue();
                target.setPort(port);
            }

            if (json.get("username") != null) {
                String username = json.get("username").isString().stringValue();
                target.setUserName(username);
            }

            if (json.get("password") != null) {
                String password = json.get("password").isString().stringValue();
                target.setPassword(password);
            }

            target.setDirty(false);
        } catch (Exception e) {
            Log.error(this.getClass(), "Unable to parse recipe JSON. " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void updateTargetFields(SshMachineTarget target) {
        this.setTargetName(target.getName());
        this.setHost(target.getHost());
        this.setPort(target.getPort());
        this.setUserName(target.getUserName());
        this.setPassword(target.getPassword());
        this.selectTargetName();
    }

    @Override
    public void enableConnectButton(boolean enable) {
        connectButton.setEnabled(enable);
    }

    @Override
    public void setConnectButtonText(String title) {
        if (title == null || title.isEmpty()) {
            connectButton.setText("");
            connectButton.setHTML("<i></i>");
        } else {
            connectButton.setText(title);
        }
    }

    @Override
    public void selectTargetName() {
        targetName.setFocus(true);
        targetName.selectAll();
    }


    interface SshViewImplUiBinder extends UiBinder<FlowPanel, SshViewImpl> {
    }
}
