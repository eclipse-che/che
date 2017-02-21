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
package org.eclipse.che.plugin.sample.perspective.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 *
 */
@Singleton
public class SampleViewImpl extends Composite implements SampleView, PartStackView {

    private LocalizationConstant constant;
    private NotificationManager notificationManager;

    interface SampleViewUiBinder extends UiBinder<Widget, SampleViewImpl> {
    }

    private final static SampleViewUiBinder UI_BINDER = GWT.create(SampleViewUiBinder.class);

    @UiField
    Button button;

    @UiField
    FlowPanel centralPart;

    @Inject
    public SampleViewImpl(LocalizationConstant constant, NotificationManager notificationManager) {
        this.constant = constant;
        this.notificationManager = notificationManager;
        initWidget(UI_BINDER.createAndBindUi(this));
        button.setText(constant.sayHello());
        button.addClickHandler(clickEvent -> notificationManager.notify(constant.hello(), SUCCESS, EMERGE_MODE));
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        asWidget().setVisible(true);
        centralPart.getElement().getParentElement().setAttribute("style", "{position:static}");

    }

    /** {@inheritDoc} */
    @Override
    public void addTab(@NotNull TabItem tabItem, @NotNull PartPresenter presenter) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void removeTab(@NotNull PartPresenter partPresenter) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void selectTab(@NotNull PartPresenter partPresenter) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setTabPositions(List<PartPresenter> partPositions) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus(boolean focused) {
        //to do nothing
    }

    @Override
    public void setMaximized(boolean maximized) {
        getElement().setAttribute("maximized", "" + maximized);
    }

    /** {@inheritDoc} */
    @Override
    public void updateTabItem(@NotNull PartPresenter partPresenter) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        //to do nothing
    }
}