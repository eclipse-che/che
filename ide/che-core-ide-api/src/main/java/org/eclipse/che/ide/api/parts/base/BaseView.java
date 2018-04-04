/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.parts.base;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.Focusable;
import org.eclipse.che.ide.util.UIUtil;

/**
 * Base view for part. By default the view has toolbar containing part description and minimize
 * button. Toolbar is represented as dock panel and can be simply expanded.
 *
 * @author Codenvy crowd
 */
public abstract class BaseView<T extends BaseActionDelegate> extends Composite
    implements View<T>, Focusable {

  /** Root widget */
  private DockLayoutPanel container;

  protected T delegate;

  protected FocusWidget lastFocused;

  /** Indicates whether this view is focused */
  private boolean focused = false;

  private BlurHandler blurHandler =
      new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
          if (event.getSource() instanceof FocusWidget) {
            lastFocused = (FocusWidget) event.getSource();
          }
        }
      };

  public BaseView() {
    container = new DockLayoutPanel(Style.Unit.PX);
    container.getElement().setAttribute("role", "part");
    container.setSize("100%", "100%");
    container.getElement().getStyle().setOutlineStyle(Style.OutlineStyle.NONE);
    initWidget(container);
  }

  /** {@inheritDoc} */
  @Override
  public final void setDelegate(T delegate) {
    this.delegate = delegate;
  }

  /**
   * Sets content widget.
   *
   * @param widget content widget
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
   * @param title part title
   */
  @Override
  public void setTitle(@NotNull String title) {}

  /** {@inheritDoc} */
  @Override
  public final void setFocus(boolean focused) {
    this.focused = focused;
    if (focused) {
      Scheduler.get().scheduleDeferred(this::focusView);
    } else {
      Scheduler.get().scheduleDeferred(this::blurView);
    }
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isFocused() {
    return focused;
  }

  /**
   * Override this method to set focus to necessary element inside the view. Method is called when
   * focusing the part view.
   */
  protected void focusView() {
    getElement().focus();
  }

  /** Handles loosing the focus. */
  protected void blurView() {
    getElement().blur();
  }
}
