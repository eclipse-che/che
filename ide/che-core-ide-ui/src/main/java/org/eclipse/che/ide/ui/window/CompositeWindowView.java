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

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Position.FIXED;
import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.user.client.Window.getClientWidth;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.ui.button.ButtonAlignment;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.util.UIUtil;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Window view implementor. Responsible for constructing the popup windows based on the rules
 * provided by {@link Window} class.
 *
 * @author Vlad Zhukovskyi
 * @since 6.0.0
 * @see Window
 * @see WindowView
 */
public class CompositeWindowView extends Composite implements WindowView {

  private final WindowClientBundle clientBundle = GWT.create(ResourceClientBundle.class);

  private VerticalPanel windowFrame;
  private HorizontalPanel windowFrameTitleBar;
  private SVGImage windowFrameCloseButton;
  private FlowPanel windowFrameBody;
  private Label windowFrameHeaderTitle;
  private FlowPanel windowFrameButtonBar;

  private Widget focusWidget;
  private Widget contentWidget;
  private boolean closeOnEscape = true;

  private int windowWidth;
  private int clientLeft;
  private int clientTop;
  private boolean dragging;
  private int dragStartX;
  private int dragStartY;
  private String transition;

  private boolean windowFrameModal = true;
  private HTMLPanel windowFrameGlassPanel = null;

  private List<BrowserEventHandler> browserEventHandlers = null;
  private List<WindowCloseEventHandler> windowCloseEventHandlers = null;

  CompositeWindowView() {
    windowWidth = getClientWidth();
    clientLeft = Document.get().getBodyOffsetLeft();
    clientTop = Document.get().getBodyOffsetTop();

    initView();
    initEventHandlers();

    sinkEvents(Event.ONMOUSEDOWN | Event.KEYEVENTS | Event.TOUCHEVENTS);

    getElement().setTabIndex(0);
    getElement().setAttribute("hideFocus", "true");
  }

  private void initView() {
    WindowClientBundle.Style style = clientBundle.getStyle();

    windowFrame = new VerticalPanel();
    windowFrame.addStyleName(style.windowFrame());

    windowFrameTitleBar = new HorizontalPanel();
    windowFrameTitleBar.addStyleName(style.windowFrameTitleBar());

    windowFrameHeaderTitle = new Label();
    windowFrameHeaderTitle.addStyleName(style.windowFrameTitle());

    windowFrameCloseButton = new SVGImage(clientBundle.closeIcon());
    windowFrameCloseButton.addClassNameBaseVal(style.windowFrameCloseButton());

    windowFrameBody = new FlowPanel();
    windowFrameBody.addStyleName(style.windowFrameBody());

    windowFrameButtonBar = new FlowPanel();
    windowFrameButtonBar.addStyleName(style.windowFrameButtonBar());
    windowFrameButtonBar.setVisible(false);

    windowFrameTitleBar.add(windowFrameHeaderTitle);
    windowFrameTitleBar.add(windowFrameCloseButton);

    windowFrame.add(windowFrameTitleBar);
    windowFrame.add(windowFrameBody);
    windowFrame.add(windowFrameButtonBar);

    initWidget(windowFrame);
  }

  @Override
  protected void onLoad() {
    if (windowFrameModal) {
      addModality();
    }
  }

  @Override
  protected void onUnload() {
    removeModality();
  }

  @Override
  public void setDebugId(String debugId) {
    ensureDebugId(debugId);

    windowFrameTitleBar.ensureDebugId(debugId + "-windowFrameTitleBar");
    windowFrameHeaderTitle.ensureDebugId(debugId + "-windowFrameTitle");
    windowFrameCloseButton.ensureDebugId(debugId + "-windowFrameCloseButton");
    windowFrameBody.ensureDebugId(debugId + "-windowFrameBody");
    windowFrameButtonBar.ensureDebugId(debugId + "-windowFrameButtonBar");
  }

  @Override
  public Widget getFocusWidget() {
    if (focusWidget == null) {
      focusWidget = windowFrame;
    }

    return focusWidget;
  }

  @Override
  public void setFocusWidget(Widget focusWidget) {
    if (focusWidget != null) {
      this.focusWidget = focusWidget;
    } else {
      this.focusWidget = windowFrame;
    }
  }

  @Override
  public void setZIndex(int zIndex) {
    if (windowFrameModal && windowFrameGlassPanel != null) {
      windowFrameGlassPanel.getElement().getStyle().setZIndex(zIndex - 1);
    }
    windowFrame.getElement().getStyle().setZIndex(zIndex);
  }

  @Override
  public void setActive(boolean active) {}

  @Override
  public void addBrowserEventHandler(BrowserEventHandler handler) {
    if (browserEventHandlers == null) {
      browserEventHandlers = new ArrayList<>();
    }

    browserEventHandlers.add(handler);
  }

  @Override
  public void addWindowCloseEventHandler(WindowCloseEventHandler handler) {
    if (windowCloseEventHandlers == null) {
      windowCloseEventHandlers = new ArrayList<>();
    }

    windowCloseEventHandlers.add(handler);
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    if (browserEventHandlers != null) {
      browserEventHandlers.forEach(handler -> handler.onBrowserEvent(event));
    }
  }

  @Override
  public void setCloseOnEscape(boolean closeOnEscape) {
    this.closeOnEscape = closeOnEscape;
  }

  @Override
  public boolean isCloseOnEscape() {
    return closeOnEscape;
  }

  @Override
  public void setContentWidget(Widget widget) {
    this.contentWidget = widget;

    windowFrameBody.clear();
    windowFrameBody.add(widget);
  }

  @Override
  public Widget getContentWidget() {
    return contentWidget;
  }

  @Override
  public void setTitle(String title) {
    super.setTitle(title);

    windowFrameHeaderTitle.setText(title);
  }

  @Override
  public void setModal(boolean modal) {
    windowFrameModal = modal;

    if (modal && windowFrameGlassPanel == null) {
      addModality();
    } else if (!modal && windowFrameGlassPanel != null) {
      removeModality();
    }
  }

  private void initEventHandlers() {
    windowFrameCloseButton.addDomHandler(
        this::onCloseButtonMouseDownEvent, MouseDownEvent.getType());
    windowFrameCloseButton.addDomHandler(this::onCloseButtonClickEvent, ClickEvent.getType());

    MouseHandler mouseHandler = new MouseHandler();
    windowFrameTitleBar.addDomHandler(mouseHandler, MouseDownEvent.getType());
    windowFrameTitleBar.addDomHandler(mouseHandler, MouseUpEvent.getType());
    windowFrameTitleBar.addDomHandler(mouseHandler, MouseMoveEvent.getType());
  }

  private void onCloseButtonClickEvent(ClickEvent event) {
    event.stopPropagation();

    if (windowCloseEventHandlers != null) {
      windowCloseEventHandlers.forEach(WindowCloseEventHandler::onClose);
    }
  }

  private void onCloseButtonMouseDownEvent(MouseDownEvent event) {
    event.preventDefault();
    event.stopPropagation();
  }

  @Override
  public Button addButtonBarControl(
      String text,
      String debugId,
      ClickHandler clickHandler,
      boolean primary,
      ButtonAlignment alignment) {
    Button button = new Button();

    if (!isNullOrEmpty(text)) {
      button.setText(text);
    }

    if (!isNullOrEmpty(debugId)) {
      button.ensureDebugId(debugId);
      button.getElement().setId(debugId);
    }

    if (clickHandler != null) {
      button.addClickHandler(clickHandler);
    }

    if (alignment != null) {
      switch (alignment) {
        case LEFT:
          button.addStyleName(clientBundle.getStyle().windowFrameFooterButtonLeft());
          break;
        case RIGHT:
        default:
          button.addStyleName(clientBundle.getStyle().windowFrameFooterButtonRight());
      }
    }

    if (primary) {
      button.addStyleName(clientBundle.getStyle().windowFrameFooterButtonPrimary());
    } else {
      button.addStyleName(clientBundle.getStyle().windowFrameFooterButton());
    }

    windowFrameButtonBar.add(button);

    if (!windowFrameButtonBar.isVisible()) {
      windowFrameButtonBar.setVisible(true);
    }

    return button;
  }

  @Override
  public void addButtonBarWidget(Widget widget) {
    windowFrameButtonBar.add(widget);

    if (!windowFrameButtonBar.isVisible()) {
      windowFrameButtonBar.setVisible(true);
    }
  }

  @Override
  public void addKeyboardNavigationHandler(KeyboardNavigationHandler handler) {
    handler.bind(windowFrame);
  }

  @Override
  public void attach() {
    if (!isAttached()) {
      RootPanel.get().add(this);
    }

    centerWindow();
    setVisible(true);
  }

  @Override
  public void detach() {
    removeFromParent();
  }

  private void centerWindow() {
    int left = (Document.get().getClientWidth() - windowFrame.getOffsetWidth()) / 2;
    int top = (Document.get().getClientHeight() - windowFrame.getOffsetHeight()) / 2;
    setPopupPosition(left, top);

    windowFrame.getElement().getStyle().setPosition(ABSOLUTE);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    if (visible) {
      getElement().getStyle().setOpacity(1.);
      getElement().getStyle().setProperty("MozTransform", "scale(1.0)");
      getElement().getStyle().setProperty("WebkitTransform", "scale(1.0)");
      getElement().getStyle().setProperty("transform", "scale(1.0)");
    }
  }

  private void addModality() {
    windowFrameGlassPanel = new HTMLPanel("div", "");

    Style style = windowFrameGlassPanel.getElement().getStyle();
    style.setPosition(FIXED);
    style.setWidth(100., PCT);
    style.setHeight(100., PCT);
    style.setTop(0., PX);
    style.setLeft(0., PX);

    RootPanel.get().add(windowFrameGlassPanel);
  }

  private void removeModality() {
    if (windowFrameGlassPanel != null) {
      windowFrameGlassPanel.removeFromParent();
      windowFrameGlassPanel = null;
    }
  }

  private void endDragging(MouseUpEvent event) {
    dragging = false;
    DOM.releaseCapture(windowFrameTitleBar.getElement());
    elemental.dom.Element element = (elemental.dom.Element) windowFrame.getElement();
    element.getStyle().setProperty("transition", transition);
  }

  private void continueDragging(MouseMoveEvent event) {
    if (dragging) {
      int absX = event.getX() + windowFrame.getAbsoluteLeft();
      int absY = event.getY() + windowFrame.getAbsoluteTop();

      // if the mouse is off the screen to the left, right, or top, don't
      // move the dialog box. This would let users lose dialog boxes, which
      // would be bad for modal popups.
      if (absX < clientLeft || absX >= windowWidth || absY < clientTop) {
        return;
      }

      setPopupPosition(absX - dragStartX, absY - dragStartY);
    }
  }

  private void beginDragging(MouseDownEvent event) {
    if (DOM.getCaptureElement() == null) {
      /*
       * Need to check to make sure that we aren't already capturing an element
       * otherwise events will not fire as expected. If this check isn't here,
       * any class which extends custom button will not fire its click event for
       * example.
       */
      dragging = true;
      DOM.setCapture(windowFrameTitleBar.getElement());
      if ("".equals(windowFrame.getElement().getStyle().getPosition())) {
        windowFrame.getElement().getStyle().setTop(windowFrame.getAbsoluteTop() + 1, PX);
        windowFrame.getElement().getStyle().setLeft(windowFrame.getAbsoluteLeft(), PX);
      } else {
        windowFrame.getElement().getStyle().setTop(windowFrame.getAbsoluteTop(), PX);
        windowFrame.getElement().getStyle().setLeft(windowFrame.getAbsoluteLeft(), PX);
      }

      windowFrame.getElement().getStyle().setPosition(ABSOLUTE);
      elemental.dom.Element element = (elemental.dom.Element) windowFrame.getElement();
      transition = element.getStyle().getPropertyValue("transition");
      element.getStyle().setProperty("transition", "all 0ms");

      dragStartX = event.getX();
      dragStartY = event.getY();
    }
  }

  /**
   * Sets the popup's position relative to the browser's client area. The popup's position may be
   * set before calling .
   *
   * @param left the left position, in pixels
   * @param top the top position, in pixels
   */
  public void setPopupPosition(int left, int top) {
    // Account for the difference between absolute position and the
    // body's positioning context.
    left -= Document.get().getBodyOffsetLeft();
    top -= Document.get().getBodyOffsetTop();

    // Set the popup's position manually, allowing setPopupPosition() to be
    // called before show() is called (so a popup can be positioned without it
    // 'jumping' on the screen).
    Element elem = windowFrame.getElement();
    elem.getStyle().setPropertyPx("left", left);
    elem.getStyle().setPropertyPx("top", top);
  }

  private class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {

    public void onMouseDown(MouseDownEvent event) {
      beginDragging(event);
    }

    public void onMouseMove(MouseMoveEvent event) {
      continueDragging(event);
    }

    public void onMouseUp(MouseUpEvent event) {
      endDragging(event);
    }
  }

  /**
   * Sets focus on the first child of content panel if such exists. Otherwise sets focus on first
   * child of footer
   */
  public void setFocus() {
    if (!setFocusOnChildOf(windowFrameBody)) {
      setFocusOnChildOf(windowFrameButtonBar);
    }
  }

  /**
   * Sets focus on the first focusable child if such exists.
   *
   * @return <code>true</code> if the focus was set
   */
  private boolean setFocusOnChildOf(Widget widget) {
    List<FocusWidget> focusableChildren = UIUtil.getFocusableChildren(widget);
    for (FocusWidget focusableWidget : focusableChildren) {
      if (focusableWidget.isVisible()) {
        focusableWidget.setFocus(true);
        return true;
      }
    }
    return false;
  }

  public interface ResourceClientBundle extends WindowClientBundle {

    @Source({
      "org/eclipse/che/ide/ui/constants.css",
      "CompositeWindowView.css",
      "org/eclipse/che/ide/api/ui/style.css"
    })
    @Override
    Style getStyle();

    @Source("close-icon.svg")
    @Override
    SVGResource closeIcon();
  }
}
