/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.window;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import com.google.web.bindery.event.shared.EventBus;
import elemental.js.dom.JsElement;
import javax.inject.Inject;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.ui.button.ButtonAlignment;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.window.event.WindowOpenedEvent;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Base class to create a window based panel with user defined widgets. In general, window contains
 * of three parts: frame title, frame content and frame bottom bar.
 *
 * <p>Frame title contains a visible title of the window. To setup a custom title method {@link
 * #setTitle(String)} has to be called. The best place to call this method is the constructor of
 * implemented class.
 *
 * <p>Frame content consists of user defined widget represented by {@link Widget}. Window adopts its
 * own size by user defined widget. To place user defined widget into window, method {@link
 * #setWidget(Widget)} has to be called.
 *
 * <p>Frame bottom bar may include that resides below the content area and includes controls for
 * affecting the content of the window. Usually bar contains buttons to accept or perform specific
 * actions. To add a predefined type of control, e.g. button, method {@link #addFooterButton(String,
 * String, ClickHandler, boolean, ButtonAlignment)} should be called. To place custom widget into
 * button bar, method {@link #addFooterWidget(Widget)} should be called. If button bar doesn't
 * contain any control, it will be hidden automatically.
 *
 * <p>By default window listens to the keyPress event to be able to close itself when user press
 * Escape key. To disable it method {@link #setCloseOnEscape(boolean)} should be called.
 *
 * @since 6.0.0
 * @author Vlad Zhukovskyi
 */
public abstract class Window implements IsWidget {

  private final WindowView view;
  private final WindowManager windowManager;
  private KeyBindingAgent keyBinding;
  private EventBus eventBus;

  public Window() {
    windowManager = WindowManager.getInstance();

    view = new CompositeWindowView();
    view.addBrowserEventHandler(this::onBrowserEvent);
    view.addWindowCloseEventHandler(this::onWindowClose);
    view.addKeyboardNavigationHandler(new ViewKeyboardNavigationHandler());
  }

  @Inject
  protected void setKeyBinding(KeyBindingAgent keyBinding) {
    this.keyBinding = keyBinding;
  }

    @Inject
    protected void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

  // Configuration section

  /**
   * Set the title to the current window.
   *
   * @param title window title
   */
  protected final void setTitle(String title) {
    view.setTitle(title);
  }

  /**
   * Set user defined widget to show it in the window.
   *
   * @param widget widget to show
   */
  protected final void setWidget(Widget widget) {
    view.setContentWidget(widget);
  }

  /**
   * Set the debug ID on the window implementation. Window implementation may use debug ID in other
   * internal components such as buttons, content container, etc.
   *
   * @param id debug id
   */
  protected final void ensureDebugId(String id) {
    view.setDebugId(id);
  }

  /**
   * Allow window listen to Escape key press to close the window. By default window is allowed to be
   * closed by Escape key.
   *
   * @param closeOnEscape {@code true} if window is allowed to be closed by Escape key press
   */
  protected final void setCloseOnEscape(boolean closeOnEscape) {
    view.setCloseOnEscape(closeOnEscape);
  }

  /**
   * Provide property to set up current window as modal. By default, window is modal.
   *
   * @param modal {@code true} if window has to be modal
   */
  protected final void setModal(boolean modal) {
    view.setModal(modal);
  }

  /**
   * Create a button in button bar. Button can be a primary. Primary flag set the different style
   * for the button to mark it from other ones.
   *
   * @param text button caption
   * @param debugId debug identifier
   * @param clickHandler handler to process click operation
   * @param primary mark button with different style
   * @param alignment button alignment
   * @return created instance of {@link Button}
   */
  protected final Button addFooterButton(
      String text,
      String debugId,
      ClickHandler clickHandler,
      boolean primary,
      ButtonAlignment alignment) {
    return view.addButtonBarControl(text, debugId, clickHandler, primary, alignment);
  }

  /**
   * Create a button in button bar. Button can be a primary. Primary flag set the different style
   * for the button to mark it from other ones.
   *
   * @param text button caption
   * @param debugId debug identifier
   * @param clickHandler handler to process click operation
   * @param primary mark button with different style
   * @return created instance of {@link Button}
   */
  protected final Button addFooterButton(
      String text, String debugId, ClickHandler clickHandler, boolean primary) {
    return addFooterButton(text, debugId, clickHandler, primary, ButtonAlignment.RIGHT);
  }

  /**
   * Create a button in button bar.
   *
   * @param text button caption
   * @param debugId debug identifier
   * @param clickHandler handler to process click operation
   * @param alignment button alignment
   * @return created instance of {@link Button}
   */
  protected final Button addFooterButton(
      String text, String debugId, ClickHandler clickHandler, ButtonAlignment alignment) {
    return addFooterButton(text, debugId, clickHandler, false, alignment);
  }

  /**
   * Create a button in button bar.
   *
   * @param text button caption
   * @param debugId debug identifier
   * @param clickHandler handler to process click operation
   * @return created instance of {@link Button}
   */
  protected final Button addFooterButton(String text, String debugId, ClickHandler clickHandler) {
    return addFooterButton(text, debugId, clickHandler, false, ButtonAlignment.RIGHT);
  }

  /**
   * Place the user defined widget into button bar. Widget may represent different type of control,
   * e.g. checkbox, help indicator, etc.
   *
   * <p>User widget places into the left part of the button bar.
   *
   * @param widget user defined widget
   */
  protected final void addFooterWidget(Widget widget) {
    view.addButtonBarWidget(widget);
  }

  /**
   * Place the user defined widget into button bar. Widget may represent different type of control,
   * e.g. checkbox, help indicator, etc.
   *
   * <p>User widget places into the left part of the button bar.
   *
   * @param widget user defined widget
   */
  protected final void addFooterWidget(IsWidget widget) {
    addFooterWidget(widget.asWidget());
  }

  // Window control

  /**
   * Remove the current window from the DOM and hide it. After window hide, method {@link #onHide()}
   * is called to allow user to perform some actions after hide.
   */
  public final void hide() {
    view.detach();
    windowManager.unregister(this);

    if (keyBinding != null) {
      keyBinding.enable();
    }

    onHide();
  }

  /** Display current window on the viewport. */
  public final void show() {
    show(null);
  }

  /**
   * Display current window on the viewport and set up focus on given {@code focusOn} widget.
   *
   * @param focusOn widget to focus
   */
  public final void show(Widget focusOn) {
    windowManager.register(this);
    view.attach();
    view.setFocusWidget(focusOn);

    if (keyBinding != null) {
      keyBinding.disable();
    }

    onShow();

    windowManager.bringToFront(this);
  }

  /**
   * Set focus to current window. Current method is not intended to be called by user. Service
   * method used by {@link WindowManager} to control the focus between window switch.
   */
  protected final void focus() {
    Scheduler.get().scheduleFinally(this::doFocus);
  }

  /** Perform user actions after widget show. */
  protected void onShow() {
      eventBus.fireEvent(new WindowOpenedEvent());
  }

  /** Perform user actions after widget hide. */
  protected void onHide() {}

  // Service methods

  @Override
  public Widget asWidget() {
    return view.getContentWidget();
  }

  private void doFocus() {
    FocusImpl.getFocusImplForWidget().focus(getFocusEl());
  }

  private Element getFocusEl() {
    Widget focusWidget = view.getFocusWidget();

    return focusWidget.getElement();
  }

  private void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
        Window activeWindow = windowManager.getActive();
        if (activeWindow != null && activeWindow != this) {
          windowManager.bringToFront(this);
        }
        break;
    }
  }

  private void onWindowClose() {
    hide();
  }

  protected final void setZIndex(int zIndex) {
    view.setZIndex(zIndex);
  }

  protected final void setActive(boolean active) {
    view.setActive(active);
  }

  public void onAltPress(NativeEvent evt) {}

  public void onBackspacePress(NativeEvent evt) {}

  public void onControlPress(NativeEvent evt) {}

  public void onDeletePress(NativeEvent evt) {}

  public void onDownPress(NativeEvent evt) {}

  public void onEndPress(NativeEvent evt) {}

  public void onEnterPress(NativeEvent evt) {}

  public void onEscPress(NativeEvent evt) {}

  public void onHomePress(NativeEvent evt) {}

  public void onKeyPress(NativeEvent evt) {}

  public void onLeftPress(NativeEvent evt) {}

  public void onPageDownPress(NativeEvent evt) {}

  public void onPageUpPress(NativeEvent evt) {}

  public void onRightPress(NativeEvent evt) {}

  public void onShiftPress(NativeEvent evt) {}

  public void onTabPress(NativeEvent evt) {}

  public void onUpPress(NativeEvent evt) {}

  private class ViewKeyboardNavigationHandler extends KeyboardNavigationHandler {
    @Override
    public void onAlt(NativeEvent evt) {
      onAltPress(evt);
    }

    @Override
    public void onBackspace(NativeEvent evt) {
      onBackspacePress(evt);
    }

    @Override
    public void onControl(NativeEvent evt) {
      onControlPress(evt);
    }

    @Override
    public void onDelete(NativeEvent evt) {
      onDeletePress(evt);
    }

    @Override
    public void onDown(NativeEvent evt) {
      onDownPress(evt);
    }

    @Override
    public void onEnd(NativeEvent evt) {
      onEndPress(evt);
    }

    @Override
    public void onEnter(NativeEvent evt) {
      onEnterPress(evt);
    }

    @Override
    public void onEsc(NativeEvent evt) {
      if (view.isCloseOnEscape()) {
        hide();
      }

      onEscPress(evt);
    }

    @Override
    public void onHome(NativeEvent evt) {
      onHomePress(evt);
    }

    @Override
    public void onKeyPress(NativeEvent evt) {
      Window.this.onKeyPress(evt);
    }

    @Override
    public void onLeft(NativeEvent evt) {
      onLeftPress(evt);
    }

    @Override
    public void onPageDown(NativeEvent evt) {
      onPageDownPress(evt);
    }

    @Override
    public void onPageUp(NativeEvent evt) {
      onPageUpPress(evt);
    }

    @Override
    public void onRight(NativeEvent evt) {
      onRightPress(evt);
    }

    @Override
    public void onShift(NativeEvent evt) {
      onShiftPress(evt);
    }

    @Override
    public void onTab(NativeEvent evt) {
      onTabPress(evt);
    }

    @Override
    public void onUp(NativeEvent evt) {
      onUpPress(evt);
    }
  }
}
