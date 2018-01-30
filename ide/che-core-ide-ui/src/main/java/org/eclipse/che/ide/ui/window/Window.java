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
package org.eclipse.che.ide.ui.window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import elemental.js.dom.JsElement;
import javax.inject.Inject;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.ui.button.ButtonAlignment;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * A popup that automatically centers its content, even if the dimensions of the content change. The
 * centering is done in CSS, so performance is very good. A semi-transparent "glass" panel appears
 * behind the popup. The glass is not optional due to the way {@link Window} is implemented.
 *
 * <p>
 *
 * <p>{@link Window} animates into and out of view using the shrink in/expand out animation.
 */
public abstract class Window implements IsWidget {

  protected static final Resources resources = GWT.create(Resources.class);

  private boolean blocked = false;

  private boolean hideOnEscapeEnabled = true;

  private boolean isShowing;
  private View view;
  private KeyBindingAgent keyBinding;

  protected Window() {
    this(true);
  }

  protected Window(boolean showBottomPanel) {
    view = new View(resources, showBottomPanel);
  }

  @Inject
  protected void setKeyBinding(KeyBindingAgent keyBinding) {
    this.keyBinding = keyBinding;
  }

  public Widget getWidget() {
    return view.getContent();
  }

  public void setWidget(Widget widget) {
    view.addContentWidget(widget);
    handleViewEvents();
  }

  /**
   * ensureDebugId on the current window container. ensureDebugId id + "-headerLabel" on the window
   * control bar title
   *
   * @see UIObject#ensureDebugId(String)
   */
  public void ensureDebugId(String id) {
    view.contentContainer.ensureDebugId(id);
    view.headerLabel.ensureDebugId(id + "-headerLabel");
  }

  public void hideCrossButton() {
    view.closeButton.setVisible(false);
  }

  /** Blocks the window, prevents it closing. */
  public void setBlocked(boolean blocked) {
    this.blocked = blocked;

    if (blocked) {
      view.closeButton.getElement().getStyle().setProperty("opacity", "0.3");
      view.closeButton.getElement().setAttribute("blocked", "");
    } else {
      view.closeButton.getElement().getStyle().clearProperty("opacity");
      view.closeButton.getElement().removeAttribute("blocked");
    }
  }

  /** Hides the {@link Window} popup. The popup will animate out of view. */
  public void hide() {
    if (blocked) {
      return;
    }

    if (!isShowing) {
      return;
    }

    if (keyBinding != null) {
      keyBinding.enable();
    }

    isShowing = false;

    // Animate the popup out of existence.
    view.setShowing(false);

    // Remove the popup when the animation completes.
    new Timer() {
      @Override
      public void run() {
        if (blocked) {
          return;
        }

        // The popup may have been shown before this timer executes.
        if (!isShowing) {
          view.removeFromParent();
          Style style = view.contentContainer.getElement().getStyle();
          style.clearPosition();
          style.clearLeft();
          style.clearTop();
        }
      }
    }.schedule(view.getAnimationDuration());
  }

  /**
   * Checks if the {@link Window} is showing or animating into view.
   *
   * @return true if showing, false if hidden
   */
  public boolean isShowing() {
    return isShowing;
  }

  /**
   * Sets whether or not the popup should hide when escape is pressed. The default behavior is to
   * ignore the escape key.
   *
   * @param isEnabled true to close on escape, false not to
   */
  public void setHideOnEscapeEnabled(boolean isEnabled) {
    this.hideOnEscapeEnabled = isEnabled;
  }

  protected Button createButton(String title, String debugId, ClickHandler clickHandler) {
    return createButton(title, debugId, clickHandler, ButtonAlignment.RIGHT);
  }

  protected Button createButton(
      String title, String debugId, ClickHandler clickHandler, ButtonAlignment alignment) {
    Button button = new Button();
    button.setText(title);
    button.ensureDebugId(debugId);
    button.getElement().setId(debugId);
    button.addStyleName(resources.windowCss().button());
    addButtonAlignment(button, alignment);
    button.addClickHandler(clickHandler);
    // set default tab index
    button.setTabIndex(0);
    return button;
  }

  protected Button createPrimaryButton(String title, String debugId, ClickHandler clickHandler) {
    return createPrimaryButton(title, debugId, clickHandler, ButtonAlignment.RIGHT);
  }

  protected Button createPrimaryButton(
      String title, String debugId, ClickHandler clickHandler, ButtonAlignment alignment) {
    Button button = createButton(title, debugId, clickHandler);
    button.addStyleName(resources.windowCss().primaryButton());
    addButtonAlignment(button, alignment);
    // set default tab index
    button.setTabIndex(0);
    return button;
  }

  protected void addButtonToFooter(Button button) {
    button.addStyleName(resources.windowCss().alignBtn());
    getFooter().add(button);
  }

  protected void onEnterClicked() {}

  /** Set focus to current window. */
  public void focus() {
    view.setFocus();
  }

  /** Sets focus on the last focused child element if such exists. */
  public void focusLastFocusedElement() {
    view.focusLastFocusedElement();
  }

  /** Returns {@code true} if widget is in the focus and {@code false} - otherwise. */
  public boolean isWidgetFocused(FocusWidget widget) {
    return view.isElementFocused(widget.getElement());
  }

  /** See {@link #show(Focusable)}. */
  public void show() {
    show(null);
  }

  /**
   * Displays the {@link Window} popup. The popup will animate into view.
   *
   * @param selectAndFocusElement an {@link Focusable} to select and focus on when the panel is
   *     shown. If null, no element will be given focus
   */
  public void show(@Nullable final Focusable selectAndFocusElement) {
    setBlocked(false);

    if (isShowing) {
      setFocusOn(
          selectAndFocusElement); // the window is displayed but focus for the element may be lost
      return;
    }

    if (keyBinding != null) {
      keyBinding.disable();
    }

    isShowing = true;

    // Attach the popup to the body.
    final JsElement popup = view.popup.getElement().cast();
    if (popup.getParentElement() == null) {
      // Hide the popup so it can enter its initial state without flickering.

      popup.getStyle().setVisibility("hidden");
      RootLayoutPanel.get().add(view);
    }

    // The popup may have been hidden before this timer executes.
    if (isShowing) {
      popup.getStyle().removeProperty("visibility");
      // Start the animation after the element is attached.
      Scheduler.get()
          .scheduleDeferred(
              new ScheduledCommand() {
                @Override
                public void execute() {
                  // The popup may have been hidden before this timer executes.
                  view.setShowing(true);
                  setFocusOn(selectAndFocusElement);
                }
              });
    }
  }

  private void addButtonAlignment(Button button, ButtonAlignment alignment) {
    switch (alignment) {
      case LEFT:
        button.addStyleName(resources.windowCss().buttonAlignLeft());
        break;
      case RIGHT:
      default:
        button.addStyleName(resources.windowCss().buttonAlignRight());
    }
  }

  /**
   * Sets focus on the given element. If {@code elementToFocus} is {@code null}, no element will be
   * given focus
   */
  private void setFocusOn(@Nullable Focusable elementToFocus) {
    if (elementToFocus != null) {
      elementToFocus.setFocus(true);
    }
  }

  private void handleViewEvents() {
    view.setDelegate(
        new ViewEvents() {
          @Override
          public void onEscapeKey() {
            Window.this.onEscapeKey();
          }

          @Override
          public void onClose() {
            if (!blocked) {
              Window.this.onClose();
            }
          }

          @Override
          public void onEnterKey() {
            onEnterClicked();
          }

          @Override
          public void onKeyDownEvent(KeyDownEvent event) {
            Window.this.onKeyDownEvent(event);
          }

          @Override
          public void onKeyPressEvent(KeyPressEvent event) {
            Window.this.onKeyPressEvent(event);
          }
        });
  }

  /** @see ViewEvents#onEscapeKey() */
  protected void onEscapeKey() {
    if (hideOnEscapeEnabled && !blocked) {
      Window.this.onClose();
    }
  }

  /** @see ViewEvents#onKeyDownEvent(KeyDownEvent) */
  protected void onKeyDownEvent(KeyDownEvent event) {}

  /** @see ViewEvents#onKeyPressEvent(KeyPressEvent) */
  protected void onKeyPressEvent(KeyPressEvent event) {}

  /** Is called when user closes the Window. */
  protected void onClose() {
    hide();
  }

  @Override
  public Widget asWidget() {
    return com.google.gwt.user.client.ui.HTML.wrap(view.getElement());
  }

  public void setTitle(String title) {
    view.headerLabel.setText(title);
  }

  public HTMLPanel getFooter() {
    return view.footer;
  }

  /** The resources used by this UI component. */
  public interface Resources extends ClientBundle {
    @Source({
      "org/eclipse/che/ide/ui/constants.css",
      "Window.css",
      "org/eclipse/che/ide/api/ui/style.css"
    })
    Css windowCss();

    @Source("close-icon.svg")
    SVGResource closeButton();
  }

  /** The Css Style names used by this panel. */
  public interface Css extends CssResource {
    /** Returns duration of the popup animation in milliseconds. */
    int animationDuration();

    String content();

    String contentVisible();

    String center();

    String glassVisible();

    String popup();

    String positioner();

    String header();

    String headerTitleWrapper();

    String headerTitleLabel();

    String footer();

    String separator();

    String alignBtn();

    String closeButton();

    String primaryButton();

    String button();

    String buttonAlignLeft();

    String buttonAlignRight();

    String image();
  }

  /** The events sources by the View. */
  public interface ViewEvents {
    /** Is called when ESCAPE key is pressed. */
    void onEscapeKey();

    void onClose();

    /** Is called when ENTER key is pressed. */
    void onEnterKey();

    /**
     * Is called when {@link KeyDownEvent} is fired except events from ESCAPE and ENTER keys. In
     * those cases {@link ViewEvents#onEscapeKey()} or {@link ViewEvents#onEnterKey()} will be
     * fired.
     */
    void onKeyDownEvent(KeyDownEvent event);

    /** Is called when {@link KeyPressEvent} is fired. */
    void onKeyPressEvent(KeyPressEvent event);
  }
}
