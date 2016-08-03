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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class SubPanelViewImpl extends Composite implements SubPanelView {

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    DockLayoutPanel mainPanel;

    @UiField
    FlowPanel tabsPanel;

    @UiField
    Button splitHorizontallyButton;

    @UiField
    Button splitVerticallyButton;

    @UiField
    Button closePaneButton;

    @UiField
    DeckLayoutPanel outputPanel;

    private ActionDelegate delegate;

    private SubPanelView parent;

    @Inject
    public SubPanelViewImpl(SubPanelViewImplUiBinder uiBinder) {
        splitLayoutPanel = new SplitLayoutPanel(5);

        initWidget(uiBinder.createAndBindUi(this));

        splitHorizontallyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSplitHorizontallyClicked();
            }
        });

        splitVerticallyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSplitVerticallyClicked();
            }
        });

        closePaneButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onClosePaneClicked();
            }
        });

        outputPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onFocused();
            }
        }, ClickEvent.getType());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void splitHorizontally(IsWidget subPanelView) {
        int height = splitLayoutPanel.getOffsetHeight() / 2;

        splitLayoutPanel.remove(mainPanel);
        splitLayoutPanel.addSouth(subPanelView, height);
        splitLayoutPanel.add(mainPanel);
    }

    @Override
    public void splitVertically(IsWidget subPanelView) {
        int width = splitLayoutPanel.getOffsetWidth() / 2;

        splitLayoutPanel.remove(mainPanel);
        splitLayoutPanel.addEast(subPanelView, width);
        splitLayoutPanel.add(mainPanel);
    }

    @Override
    public void addWidget(IsWidget widget) {
        outputPanel.setWidget(widget);
    }

    @Override
    public void activateWidget(IsWidget widget) {
        outputPanel.showWidget(widget.asWidget());
    }

    @Override
    public void removeCentralPanel() {
        if (splitLayoutPanel.getWidgetCount() == 1) {
            parent.removeChildSubPanel(this);
            return;
        }

        splitLayoutPanel.remove(mainPanel);

        Widget lastWidget = splitLayoutPanel.getWidget(0);
        splitLayoutPanel.setWidgetSize(lastWidget, 0);
        splitLayoutPanel.remove(lastWidget);
        splitLayoutPanel.add(lastWidget);
    }

    @Override
    public void removeChildSubPanel(Widget w) {
        if (splitLayoutPanel.getWidgetDirection(w) == DockLayoutPanel.Direction.CENTER) {
            // this is the only widget on the panel
            // don't allow to remove it
            return;
        }

        splitLayoutPanel.setWidgetSize(w, 0);
        splitLayoutPanel.remove(w);
    }

    @Override
    public void setParent(SubPanelView w) {
        parent = w;
    }

    @Override
    public void removeWidget(IsWidget widget) {
        outputPanel.remove(widget);
    }

    interface SubPanelViewImplUiBinder extends UiBinder<Widget, SubPanelViewImpl> {
    }
}
