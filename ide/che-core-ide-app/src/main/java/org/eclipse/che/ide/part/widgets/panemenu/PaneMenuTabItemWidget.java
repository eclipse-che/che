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
package org.eclipse.che.ide.part.widgets.panemenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.parts.PartStackView.TabItem;

import javax.validation.constraints.NotNull;

/**
 * Implementation of {@link EditorPaneMenuItem} to displaying editor tab item in {@link EditorPaneMenu}
 *
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public class PaneMenuTabItemWidget extends Composite implements EditorPaneMenuItem<TabItem> {

    interface PaneMenuTabItemWidgetUiBinder extends UiBinder<Widget, PaneMenuTabItemWidget> {
    }

    private static final PaneMenuTabItemWidgetUiBinder UI_BINDER = GWT.create(PaneMenuTabItemWidgetUiBinder.class);

    private TabItem tabItem;

    @UiField
    FlowPanel iconPanel;

    @UiField
    Label title;

    @UiField
    FlowPanel closeButton;

    private ActionDelegate<TabItem> delegate;

    public PaneMenuTabItemWidget(@NotNull TabItem tabItem) {
        initWidget(UI_BINDER.createAndBindUi(this));
        this.tabItem = tabItem;

        Widget icon = tabItem.getIcon();
        if (icon != null) {
            iconPanel.add(icon);
        }
        title.setText(tabItem.getTitle());

        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (delegate != null) {
                    delegate.onItemClicked(PaneMenuTabItemWidget.this);
                }
            }
        }, ClickEvent.getType());

        closeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                clickEvent.stopPropagation();
                clickEvent.preventDefault();

                if (delegate != null) {
                    delegate.onCloseButtonClicked(PaneMenuTabItemWidget.this);
                }
            }
        }, ClickEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate<TabItem> delegate) {
        this.delegate = delegate;
    }

    @Override
    public TabItem getData() {
        return tabItem;
    }
}
