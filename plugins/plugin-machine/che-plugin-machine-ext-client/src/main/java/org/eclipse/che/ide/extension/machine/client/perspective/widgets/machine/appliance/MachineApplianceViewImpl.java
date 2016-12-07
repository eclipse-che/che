/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.extension.machine.client.MachineResources;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The class offers special main panel to add tab container. The class is a wrapper of tab container view.
 *
 * @author Dmitry Shnurenko
 */
public class MachineApplianceViewImpl extends Composite implements MachineApplianceView, PartStackView, RequiresResize {
    interface MachineInfoContainerUiBinder extends UiBinder<Widget, MachineApplianceViewImpl> {
    }

    private final static MachineInfoContainerUiBinder UI_BINDER = GWT.create(MachineInfoContainerUiBinder.class);

    private final Label          unavailableLabel;
    private final List<IsWidget> tabContainers;

    @UiField
    FlowPanel mainContainer;

    @Inject
    public MachineApplianceViewImpl(MachineResources resources, Label unavailableLabel) {
        initWidget(UI_BINDER.createAndBindUi(this));

        this.unavailableLabel = unavailableLabel;
        this.unavailableLabel.addStyleName(resources.getCss().unavailableLabel());

        this.tabContainers = new ArrayList<>();

        addContainer(unavailableLabel);

        setMaximized(false);
    }

    /** {@inheritDoc} */
    @Override
    public void showContainer(@NotNull IsWidget tabContainer) {
        hideAllContainers();

        tabContainer.asWidget().setVisible(true);
    }

    /** {@inheritDoc} */
    @Override
    public void showStub(String message) {
        hideAllContainers();

        unavailableLabel.setText(message);

        unavailableLabel.setVisible(true);
    }

    private void hideAllContainers() {
        for (IsWidget widget : tabContainers) {
            widget.asWidget().setVisible(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addContainer(@NotNull IsWidget tabContainer) {
        if (!tabContainers.contains(tabContainer)) {
            tabContainers.add(tabContainer);

            mainContainer.add(tabContainer);
        }
    }

    @Override
    public void onResize() {
        for (int i = 0; i < mainContainer.getWidgetCount(); i++) {
            Widget widget = mainContainer.getWidget(i);
            if (widget instanceof RequiresResize) {
                ((RequiresResize)widget).onResize();
            }
        }
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