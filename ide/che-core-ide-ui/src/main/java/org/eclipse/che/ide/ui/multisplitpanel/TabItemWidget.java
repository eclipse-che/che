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
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.multisplitpanel.TabItem;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public class TabItemWidget extends Composite implements TabItem {

    private static final TabItemWidgetUiBinder UI_BINDER = GWT.create(TabItemWidgetUiBinder.class);

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
    public TabItemWidget(PartStackUIResources resources, @Assisted String title) {
        this.resources = resources;

        initWidget(UI_BINDER.createAndBindUi(this));

        titleLabel.setText(title);

        addDomHandler(this, ClickEvent.getType());

        closeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onTabClosing(TabItemWidget.this);
            }
        }, ClickEvent.getType());
    }

    @Override
    public SVGResource getIcon() {
        return null;
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
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface TabItemWidgetUiBinder extends UiBinder<Widget, TabItemWidget> {
    }
}
