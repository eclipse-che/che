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
package org.eclipse.che.ide.part.widgets.listtab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.Resources;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public class ListButtonWidget extends Composite implements ListButton {

    interface ListButtonWidgetUiBinder extends UiBinder<Widget, ListButtonWidget> {
    }

    private static final String GWT_POPUP_STANDARD_STYLE = "gwt-PopupPanel";

    private static final ListButtonWidgetUiBinder UI_BINDER = GWT.create(ListButtonWidgetUiBinder.class);

    private final PopupPanel     popupPanel;

    private final FlowPanel      listPanel;

    private final List<ListItem> items = new ArrayList<>();

    @UiField(provided = true)
    final Resources resources;

    private ActionDelegate delegate;

    private long closeTime;

    @Inject
    public ListButtonWidget(Resources resources) {
        this.resources = resources;
        initWidget(UI_BINDER.createAndBindUi(this));

        closeTime = System.currentTimeMillis();
        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                long time = System.currentTimeMillis();
                if (time - closeTime < 100) {
                    return;
                }
                showList();
            }
        }, MouseDownEvent.getType());

        listPanel = new FlowPanel();
        listPanel.addStyleName(resources.partStackCss().listItemPanel());

        popupPanel = new PopupPanel();
        popupPanel.setAutoHideEnabled(true);
        popupPanel.removeStyleName(GWT_POPUP_STANDARD_STYLE);
        popupPanel.add(listPanel);

        popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                closeTime = System.currentTimeMillis();
            }
        });
    }

    /** {@inheritDoc} */
    public void showList() {
        int x = getAbsoluteLeft() + getOffsetWidth() - 6;
        int y = getAbsoluteTop() + 19;

        popupPanel.show();
        popupPanel.getElement().getStyle().setProperty("position", "absolute");
        popupPanel.getElement().getStyle().clearProperty("left");
        popupPanel.getElement().getStyle().setProperty("right", "calc(100% - " + x + "px");
        popupPanel.getElement().getStyle().setProperty("top", "" + y + "px");
    }

    private ListItem.ActionDelegate itemDelegate = new ListItem.ActionDelegate() {
        @Override
        public void onItemClicked(@NotNull ListItem listItem) {
            popupPanel.hide();
            if (delegate != null) {
                delegate.onTabClicked(listItem.getTabItem());
            }
        }

        @Override
        public void onCloseButtonClicked(@NotNull ListItem listItem) {
            popupPanel.hide();
            if (delegate != null) {
                delegate.onTabClose(listItem.getTabItem());
            }
        }
    };

    /** {@inheritDoc} */
    @Override
    public void addListItem(@NotNull ListItem listItem) {
        items.add(listItem);
        listPanel.add(listItem);
        listItem.setDelegate(itemDelegate);
    }

    /** {@inheritDoc} */
    @Override
    public void removeListItem(@NotNull ListItem listItem) {
        items.remove(listItem);
        listPanel.remove(listItem);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

}
