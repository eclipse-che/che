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
package org.eclipse.che.ide.api.parts.base;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.Focusable;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
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

        titleLabel = new Label();
        titleLabel.setStyleName(resources.partStackCss().ideBasePartTitleLabel());

        SVGImage minimize = new SVGImage(resources.collapseExpandIcon());
        minimize.getElement().setAttribute("name", "workBenchIconMinimize");
        minimizeButton = new ToolButton(minimize);
        minimizeButton.setTitle("Hide");

        minimizeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                minimize();
            }
        });
        toolbarHeader.addWest(titleLabel, 200);

        addToolButton(minimizeButton);

        toolBar.addNorth(toolbarHeader, 22);
    }

    /**
     * Add a button on part toolbar,
     *
     * @param button button
     */
    public final void addToolButton(@NotNull IsWidget button) {
        if (button != null) {
            toolbarHeader.addEast(button, 22);
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

    /** Requests delegate to minimize the part */
    protected void minimize() {
        if (delegate != null) {
            delegate.minimize();
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
