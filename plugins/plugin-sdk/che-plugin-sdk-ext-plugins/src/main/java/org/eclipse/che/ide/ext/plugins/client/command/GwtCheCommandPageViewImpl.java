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
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ext.plugins.client.PluginsLocalizationConstant;

/**
 * The implementation of {@link GwtCheCommandPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCheCommandPageViewImpl implements GwtCheCommandPageView {

    private static final CommandPageViewImplUiBinder UI_BINDER = GWT.create(CommandPageViewImplUiBinder.class);

    private final FlowPanel rootElement;

    @UiField
    TextBox                     gwtModule;
    @UiField
    TextBox                     codeServerAddress;
    @UiField
    TextArea                    classPath;
    @UiField(provided = true)
    PluginsLocalizationConstant locale;

    private ActionDelegate delegate;

    @Inject
    public GwtCheCommandPageViewImpl(PluginsLocalizationConstant locale) {
        this.locale = locale;
        rootElement = UI_BINDER.createAndBindUi(this);
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
    public String getGwtModule() {
        return gwtModule.getValue();
    }

    @Override
    public void setGwtModule(String gwtModule) {
        this.gwtModule.setValue(gwtModule);
    }

    @Override
    public String getCodeServerAddress() {
        return codeServerAddress.getValue();
    }

    @Override
    public void setCodeServerAddress(String codeServerAddress) {
        this.codeServerAddress.setValue(codeServerAddress);
    }

    @Override
    public String getClassPath() {
        return classPath.getValue();
    }

    @Override
    public void setClassPath(String classPath) {
        this.classPath.setValue(classPath);
    }

    @UiHandler({"gwtModule"})
    void onGwtModuleChanged(KeyUpEvent event) {
        // gwtModule value may not be updated immediately after keyUp
        // therefore use the timer with delay=0
        new Timer() {
            @Override
            public void run() {
                delegate.onGwtModuleChanged();
            }
        }.schedule(0);
    }

    @UiHandler({"codeServerAddress"})
    void onCodeServerAddressChanged(KeyUpEvent event) {
        // codeServerAddress value may not be updated immediately after keyUp
        // therefore use the timer with delay=0
        new Timer() {
            @Override
            public void run() {
                delegate.onCodeServerAddressChanged();
            }
        }.schedule(0);
    }

    @UiHandler({"classPath"})
    void onClassPathChanged(KeyUpEvent event) {
        // classPath value may not be updated immediately after keyUp
        // therefore use the timer with delay=0
        new Timer() {
            @Override
            public void run() {
                delegate.onClassPathChanged();
            }
        }.schedule(0);
    }

    interface CommandPageViewImplUiBinder extends UiBinder<FlowPanel, GwtCheCommandPageViewImpl> {
    }
}
