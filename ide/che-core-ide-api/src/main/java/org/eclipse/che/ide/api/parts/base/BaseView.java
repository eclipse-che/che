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
package org.eclipse.che.ide.api.parts.base;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.Focusable;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.util.UIUtil;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;

/**
 * Base view for part. By default the view has toolbar containing part description and minimize button.
 * Toolbar is represented as dock panel and can be simply expanded.
 *
 * @author Codenvy crowd
 */
public abstract class BaseView<T extends BaseActionDelegate> extends Composite implements View<T>, Focusable {

    private final PartStackUIResources resources;

    /** Root widget */
    private DockLayoutPanel container;

    protected DockLayoutPanel toolBar;
    protected DockLayoutPanel toolbarHeader;

    protected T           delegate;
    protected ToolButton  minimizeButton;
    protected Label       titleLabel;
    protected FocusWidget lastFocused;

    /** Indicates whether this view is focused */
    private boolean focused = false;

    private BlurHandler blurHandler = new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
            if (event.getSource() instanceof FocusWidget) {
                lastFocused = (FocusWidget)event.getSource();
            }
        }
    };

    public BaseView(PartStackUIResources resources) {
        this.resources = resources;

        container = new DockLayoutPanel(Style.Unit.PX);
        container.getElement().setAttribute("role", "part");
        container.setSize("100%", "100%");
        container.getElement().getStyle().setOutlineStyle(Style.OutlineStyle.NONE);
        initWidget(container);

        toolBar = new DockLayoutPanel(Style.Unit.PX);
        toolBar.addStyleName(resources.partStackCss().ideBasePartToolbar());
        toolBar.getElement().setAttribute("role", "toolbar");
        toolBar.addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                //activate last focused element if user clicked on part header
                if (lastFocused != null) {
                    lastFocused.setFocus(true);
                }
            }
        }, MouseUpEvent.getType());
        container.addNorth(toolBar, 23);

        //this hack used for adding box shadow effect to toolbar
        toolBar.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        toolbarHeader = new DockLayoutPanel(Style.Unit.PX);
        toolbarHeader.getElement().setAttribute("role", "toolbar-header");
        toolBar.addNorth(toolbarHeader, 22);

        // padding 2 pixels from the right
        toolbarHeader.addEast(new FlowPanel(), 2);

        titleLabel = new Label();
        titleLabel.setStyleName(resources.partStackCss().ideBasePartTitleLabel());
        toolbarHeader.addWest(titleLabel, 200);

        addMaximizeButton();
        addMinimizeButton();
        addMenuButton();

        /**
         * Handle double clicking on the toolbar header
         */
        toolbarHeader.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                onToggleMaximize();
            }
        }, DoubleClickEvent.getType());
    }

    /**
     * Adds minimize part button.
     */
    private void addMinimizeButton() {
        SVGImage minimize = new SVGImage(resources.collapseExpandIcon());
        minimize.getElement().setAttribute("name", "workBenchIconMinimize");
        minimizeButton = new ToolButton(minimize);

        minimizeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onMinimize();
            }
        });

        addToolButton(minimizeButton);

        if (minimizeButton.getElement() instanceof elemental.dom.Element) {
            Tooltip.create((elemental.dom.Element) minimizeButton.getElement(),
                    PositionController.VerticalAlign.BOTTOM, PositionController.HorizontalAlign.MIDDLE, "Hide");
        }
    }

    /**
     * Adds maximize part button.
     */
    private void addMaximizeButton() {
        SVGImage maximize = new SVGImage(resources.maximizePart());
        maximize.getElement().setAttribute("name", "workBenchIconMaximize");
        ToolButton maximizeButton = new ToolButton(maximize);
        maximizeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onToggleMaximize();
            }
        });

        addToolButton(maximizeButton);

        if (maximizeButton.getElement() instanceof elemental.dom.Element) {
            Tooltip.create((elemental.dom.Element) maximizeButton.getElement(),
                    PositionController.VerticalAlign.BOTTOM, PositionController.HorizontalAlign.MIDDLE, "Maximize panel");
        }
    }

    /**
     * Adds part menu button.
     */
    private void addMenuButton() {
        final ToolButton menuButton = new ToolButton(FontAwesome.COG + "&nbsp;" + FontAwesome.CARET_DOWN);
        menuButton.getElement().setAttribute("name", "workBenchIconMenu");
        menuButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int left = getAbsoluteLeft(menuButton.getElement());
                int top = getAbsoluteTop(menuButton.getElement());
                delegate.onPartMenu(left, top + 21);
            }
        });

        toolbarHeader.addEast(menuButton, 25);

        if (menuButton.getElement() instanceof elemental.dom.Element) {
            Tooltip.create((elemental.dom.Element) menuButton.getElement(),
                    PositionController.VerticalAlign.BOTTOM, PositionController.HorizontalAlign.MIDDLE, "Panel options");
        }
    }

    /**
     * Returns absolute left position of the element.
     *
     * @param element
     *          element
     * @return
     *          element left position
     */
    private native int getAbsoluteLeft(JavaScriptObject element) /*-{
        return element.getBoundingClientRect().left;
    }-*/;

    /**
     * Returns absolute top position of the element.
     *
     * @param element
     *          element
     * @return
     *          element top position
     */
    private native int getAbsoluteTop(JavaScriptObject element) /*-{
        return element.getBoundingClientRect().top;
    }-*/;

    /**
     * Add a button on part toolbar,
     *
     * @param button button
     */
    public final void addToolButton(@NotNull IsWidget button) {
        if (button != null) {
            toolbarHeader.addEast(button, 18);
        }
    }

    /**
     * Removes button from part toolbar.
     *
     * @param button button
     */
    public void removeToolButton(@NotNull IsWidget button) {
        if (button != null) {
            toolbarHeader.remove(button);
        }
    }

    /**
     * Sets button visible on part toolbar.
     *
     * @param button button
     */
    public final void showToolButton(@NotNull IsWidget button) {
        if (button != null) {
            toolbarHeader.setWidgetHidden(button.asWidget(), false);
        }
    }

    /**
     * Hides button on part toolbar.
     *
     * @param button button
     */
    public final void hideToolButton(@NotNull IsWidget button) {
        if (button != null) {
            toolbarHeader.setWidgetHidden(button.asWidget(), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setDelegate(T delegate) {
        this.delegate = delegate;
    }

    /**
     * Toggles maximized state of the view.
     */
    public void onToggleMaximize() {
        if (delegate != null) {
            delegate.onToggleMaximize();
        }
    }

    /**
     * Minimizes the view.
     */
    public void onMinimize() {
        if (delegate != null) {
            delegate.onMinimize();
        }
    }

    /**
     * Sets content widget.
     *
     * @param widget
     *         content widget
     */
    public final void setContentWidget(Widget widget) {
        container.add(widget);
        for (FocusWidget focusWidget : UIUtil.getFocusableChildren(widget)) {
            focusWidget.addBlurHandler(blurHandler);
        }

        focusView();
    }

    /**
     * Sets new value of part title.
     *
     * @param title
     *         part title
     */
    @Override
    public void setTitle(@NotNull String title) {
        titleLabel.setText(title);
    }

    /**
     * Sets new height of the toolbar.
     *
     * @param height
     *         new toolbar height
     */
    @Deprecated
    public final void setToolbarHeight(int height) {
        container.setWidgetSize(toolBar, height);
    }

    /** {@inheritDoc} */
    @Override
    public final void setFocus(boolean focused) {
        this.focused = focused;
        if (focused) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    focusView();
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isFocused() {
        return focused;
    }

    /**
     * Override this method to set focus to necessary element inside the view.
     * Method is called when focusing the part view.
     */
    protected void focusView() {
        getElement().focus();
    }

}
