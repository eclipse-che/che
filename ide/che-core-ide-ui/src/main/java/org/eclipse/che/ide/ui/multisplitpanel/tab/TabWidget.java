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
package org.eclipse.che.ide.ui.multisplitpanel.tab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * Widget that represents a tab.
 *
 * @author Artem Zatsarynnyi
 */
public class TabWidget extends Composite implements Tab {

    private static final TabItemWidgetUiBinder UI_BINDER = GWT.create(TabItemWidgetUiBinder.class);

    private final String      title;
    private final SVGResource icon;

    @UiField
    SimplePanel iconPanel;

    @UiField
    Label titleLabel;

    @UiField
    FlowPanel closeButton;

    @UiField(provided = true)
    PartStackUIResources resources;

    private ActionDelegate delegate;

    @Inject
    public TabWidget(PartStackUIResources resources, @Assisted String title, @Assisted SVGResource icon, @Assisted boolean closable) {
        this.resources = resources;
        this.title = title;
        this.icon = icon;

        initWidget(UI_BINDER.createAndBindUi(this));

        titleLabel.setText(title);

        iconPanel.add(new SVGImage(getIcon()));

        addDomHandler(this, ClickEvent.getType());
        addDomHandler(this, DoubleClickEvent.getType());

        if (closable) {
            closeButton.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    delegate.onTabClosing(TabWidget.this);
                }
            }, ClickEvent.getType());
        } else {
            closeButton.setVisible(false);
        }
    }

    @Override
    public SVGResource getIcon() {
        return icon;
    }

    @Override
    public String getTitleText() {
        return title;
    }

    @Override
    public void select() {
        getElement().setAttribute("focused", "");
    }

    @Override
    public void unSelect() {
        getElement().removeAttribute("focused");
    }

    @Override
    public void onClick(@NotNull ClickEvent event) {
        if (NativeEvent.BUTTON_LEFT == event.getNativeButton()) {
            delegate.onTabClicked(this);
        } else if (NativeEvent.BUTTON_MIDDLE == event.getNativeButton()) {
            delegate.onTabClosing(this);
        }
    }

    @Override
    public void onDoubleClick(DoubleClickEvent event) {
        delegate.onTabDoubleClicked(this);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface TabItemWidgetUiBinder extends UiBinder<Widget, TabWidget> {
    }

}
