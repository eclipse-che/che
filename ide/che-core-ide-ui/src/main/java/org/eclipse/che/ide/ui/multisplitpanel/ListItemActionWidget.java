/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 ******************************************************************************/
package org.eclipse.che.ide.ui.multisplitpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.action.Action;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public class ListItemActionWidget extends Composite implements ListItem<Action> {

    interface ListItemWidgetUiBinder extends UiBinder<Widget, ListItemActionWidget> {
    }

    private static final ListItemWidgetUiBinder UI_BINDER = GWT.create(ListItemWidgetUiBinder.class);

    private Action action;

    @UiField
    FlowPanel iconPanel;

    @UiField
    Label title;

    @UiField
    FlowPanel closeButton;

    private ActionDelegate delegate;

    public ListItemActionWidget(Action action) {
        initWidget(UI_BINDER.createAndBindUi(this));
        this.action = action;

        Widget icon = new SVGImage(action.getTemplatePresentation().getSVGResource());
        iconPanel.add(icon);
        title.setText(action.getTemplatePresentation().getText());

        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (delegate != null) {
                    delegate.onItemClicked(ListItemActionWidget.this);
                }
            }
        }, ClickEvent.getType());

        closeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                clickEvent.stopPropagation();
                clickEvent.preventDefault();

                if (delegate != null) {
                    delegate.onCloseButtonClicked(ListItemActionWidget.this);
                }
            }
        }, ClickEvent.getType());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Action getTabItem() {
        return action;
    }
}
