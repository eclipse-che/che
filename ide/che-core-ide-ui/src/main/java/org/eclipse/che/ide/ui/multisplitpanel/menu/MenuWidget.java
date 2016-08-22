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
package org.eclipse.che.ide.ui.multisplitpanel.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Widget that represents the {@link Menu}.
 *
 * @author Artem Zatsarynnyi
 */
public class MenuWidget extends Composite implements Menu {

    private static final String                   GWT_POPUP_STANDARD_STYLE = "gwt-PopupPanel";
    private static final ListButtonWidgetUiBinder UI_BINDER                = GWT.create(ListButtonWidgetUiBinder.class);

    @UiField(provided = true)
    final Resources resources;

    private final PopupPanel popupPanel;
    private final FlowPanel  listPanel;

    private ActionDelegate delegate;
    private long           closeTime;

    private MenuItem.ActionDelegate itemDelegate = new MenuItem.ActionDelegate() {

        @Override
        public void onItemSelected(MenuItem menuItem) {
            popupPanel.hide();
            if (delegate != null) {
                delegate.onMenuItemSelected(menuItem);
            }
        }

        @Override
        public void onItemClosing(MenuItem menuItem) {
            popupPanel.hide();
            if (delegate != null) {
                delegate.onMenuItemClosing(menuItem);
            }
        }
    };

    @Inject
    public MenuWidget(Resources resources) {
        this.resources = resources;
        resources.css().ensureInjected();

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
        listPanel.addStyleName(resources.css().listItemPanel());

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

    public void showList() {
        int x = getAbsoluteLeft() + getOffsetWidth() - 6;
        int y = getAbsoluteTop() + 19;

        popupPanel.show();
        popupPanel.getElement().getStyle().setProperty("position", "absolute");
        popupPanel.getElement().getStyle().clearProperty("left");
        popupPanel.getElement().getStyle().setProperty("right", "calc(100% - " + x + "px");
        popupPanel.getElement().getStyle().setProperty("top", "" + y + "px");
    }

    @Override
    public void addListItem(MenuItem menuItem) {
        menuItem.setDelegate(itemDelegate);
        listPanel.add(menuItem);
    }

    @Override
    public void removeListItem(MenuItem menuItem) {
        listPanel.remove(menuItem);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    public interface Resources extends ClientBundle {
        @Source({"style.css", "org/eclipse/che/ide/api/ui/style.css"})
        Resources.Styles css();

        @Source("multi-file-icon.svg")
        SVGResource multiFileIcon();

        interface Styles extends CssResource {
            String listItemPanel();
        }
    }

    interface ListButtonWidgetUiBinder extends UiBinder<Widget, MenuWidget> {
    }

}
