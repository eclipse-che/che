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
package org.eclipse.che.ide.part.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenu;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public class EditorPartStackView extends ResizeComposite implements PartStackView, MouseDownHandler {

    interface PartStackUiBinder extends UiBinder<Widget, EditorPartStackView> {
    }

    private static final PartStackUiBinder UI_BINDER = GWT.create(PartStackUiBinder.class);

    @UiField
    DockLayoutPanel parent;

    @UiField
    FlowPanel tabsPanel;

    @UiField
    DeckLayoutPanel contentPanel;

    private final Map<PartPresenter, TabItem> tabs;
    private final AcceptsOneWidget            partViewContainer;
    private final LinkedList<PartPresenter>   contents;

    private ActionDelegate delegate;
    private EditorPaneMenu editorPaneMenu;
    private TabItem        activeTab;

    public EditorPartStackView() {
        this.tabs = new HashMap<>();
        this.contents = new LinkedList<>();

        initWidget(UI_BINDER.createAndBindUi(this));

        partViewContainer = new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget widget) {
                contentPanel.add(widget);
            }
        };

        addDomHandler(this, MouseDownEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttach() {
        super.onAttach();

        Style style = getElement().getParentElement().getStyle();
        style.setHeight(100, PCT);
        style.setWidth(100, PCT);
    }

    /**
     * Adds editor pane menu button in special place on view.
     *
     * @param editorPaneMenu
     *         button which will be added
     */
    public void addPaneMenuButton(@NotNull EditorPaneMenu editorPaneMenu) {
        this.editorPaneMenu = editorPaneMenu;
        tabsPanel.add(editorPaneMenu);
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseDown(@NotNull MouseDownEvent event) {
        delegate.onRequestFocus();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void addTab(@NotNull TabItem tabItem, @NotNull PartPresenter partPresenter) {
        /** Show editor area if it is empty and hidden */
        if (contents.isEmpty()) {
            getElement().getParentElement().getStyle().setDisplay(BLOCK);
        }

        /** Add editor tab to tab panel */
        tabsPanel.add(tabItem.getView());

        /** Process added editor tab */
        tabs.put(partPresenter, tabItem);
        contents.add(partPresenter);
        partPresenter.go(partViewContainer);
    }

    /**
     * Makes active tab visible.
     */
    private void ensureActiveTabVisible() {
        if (activeTab == null) {
            return;
        }

        for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
            if (editorPaneMenu != null && editorPaneMenu != tabsPanel.getWidget(i)) {
                tabsPanel.getWidget(i).setVisible(true);
            }
        }

        for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
            Widget currentWidget = tabsPanel.getWidget(i);
            Widget activeTabWidget = activeTab.getView().asWidget();
            if (editorPaneMenu != null && editorPaneMenu == currentWidget) {
                continue;
            }

            if (activeTabWidget.getAbsoluteTop() > tabsPanel.getAbsoluteTop() && activeTabWidget != currentWidget) {
                currentWidget.setVisible(false);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeTab(@NotNull PartPresenter presenter) {
        TabItem tab = tabs.get(presenter);
        tabsPanel.remove(tab.getView());
        contentPanel.remove(presenter.getView());

        tabs.remove(presenter);
        contents.remove(presenter);

        if (!contents.isEmpty()) {
            selectTab(contents.getLast());
        }

        //this hack need to force redraw dom element to apply correct styles
        tabsPanel.getElement().getStyle().setDisplay(NONE);
        tabsPanel.getElement().getOffsetHeight();
        tabsPanel.getElement().getStyle().setDisplay(BLOCK);
    }

    /** {@inheritDoc} */
    @Override
    public void selectTab(@NotNull PartPresenter partPresenter) {
        IsWidget view = partPresenter.getView();

        // set/remove attribute 'active' for Selenium tests
        for (int i = 0; i < contentPanel.getWidgetCount(); i++) {
            contentPanel.getWidget(i).getElement().removeAttribute("active");
        }
        view.asWidget().getElement().setAttribute("active", "");

        int viewIndex = contentPanel.getWidgetIndex(view);
        if (viewIndex < 0) {
            partPresenter.go(partViewContainer);
            viewIndex = contentPanel.getWidgetIndex(view);
        }

        contentPanel.showWidget(viewIndex);
        setActiveTab(partPresenter);

        if (partPresenter instanceof TextEditor) {
            ((TextEditor)partPresenter).activate();
        }
    }

    /**
     * Switches to specified tab.
     *
     * @param part tab part
     */
    private void setActiveTab(@NotNull PartPresenter part) {
        for (TabItem tab : tabs.values()) {
            tab.unSelect();
            tab.getView().asWidget().getElement().removeAttribute("active");
        }

        activeTab = tabs.get(part);
        activeTab.select();

        activeTab.getView().asWidget().getElement().setAttribute("active", "");

        delegate.onRequestFocus();

        ensureActiveTabVisible();
    }

    /** {@inheritDoc} */
    @Override
    public void setTabPositions(List<PartPresenter> partPositions) {
        throw new UnsupportedOperationException("The method doesn't allowed in this class " + getClass());
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus(boolean focused) {
        if (activeTab == null) {
            return;
        }

        if (focused) {
            activeTab.select();
        } else {
            activeTab.unSelect();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateTabItem(@NotNull PartPresenter partPresenter) {
        TabItem tab = tabs.get(partPresenter);
        tab.update(partPresenter);
    }

    @Override
    public void onResize() {
        super.onResize();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                ensureActiveTabVisible();
            }
        });
    }
}
