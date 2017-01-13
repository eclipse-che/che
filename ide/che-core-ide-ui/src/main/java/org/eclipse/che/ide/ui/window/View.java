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
package org.eclipse.che.ide.ui.window;

import elemental.events.KeyboardEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.util.UIUtil;

import java.util.List;

/**
 * The view that renders the {@link Window}. The View consists of a glass
 * panel that fades out the background, and a DOM structure that positions the
 * contents in the exact center of the screen.
 */
class View extends Composite {

    private static MyBinder uiBinder = GWT.create(MyBinder.class);
    final Window.Resources res;
    @UiField(provided = true)
    final Window.Css       css;
    @UiField
    FocusPanel focusPanel;
    @UiField
    FlowPanel  contentContainer;
    @UiField
    FlowPanel  popup;
    @UiField
    FlowPanel  header;
    @UiField
    FlowPanel  content;
    @UiField
    Label      headerLabel;

    HTMLPanel footer;
    @UiField
    FlowPanel closeButton;

    private int               windowWidth;
    private int               clientLeft;
    private int               clientTop;
    private Window.ViewEvents delegate;
    private boolean           dragging;
    private int               dragStartX;
    private int               dragStartY;
    private String            transition;
    private FocusWidget       lastFocused;

    private BlurHandler blurHandler = new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
            if (event.getSource() instanceof FocusWidget) {
                lastFocused = (FocusWidget)event.getSource();
            }
        }
    };

    View(Window.Resources res, boolean showBottomPanel) {
        this.res = res;
        this.css = res.windowCss();
        windowWidth = com.google.gwt.user.client.Window.getClientWidth();
        clientLeft = Document.get().getBodyOffsetLeft();
        clientTop = Document.get().getBodyOffsetTop();
        initWidget(uiBinder.createAndBindUi(this));
        footer = new HTMLPanel("");
        if (showBottomPanel) {
            footer.setStyleName(res.windowCss().footer());
            contentContainer.add(footer);
        }
        handleEvents();

        FocusPanel dummyFocusElement = new FocusPanel();
        dummyFocusElement.setTabIndex(0);
        dummyFocusElement.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                setFocus();
            }
        });
        contentContainer.add(dummyFocusElement);
    }

    /**
     * Returns the duration of the popup animation in milliseconds. The return
     * value should equal the value of {@link Window.Css#animationDuration()}.
     */
    protected int getAnimationDuration() {
        return css.animationDuration();
    }

    /**
     * Updates the View to reflect the showing state of the popup.
     *
     * @param showing
     *         true if showing, false if not.
     */
    protected void setShowing(boolean showing) {
        //set for each focusable widget blur handler to have ability to store last focused element
        for (FocusWidget focusWidget : UIUtil.getFocusableChildren(content)) {
            focusWidget.addBlurHandler(blurHandler);
        }

        if (showing) {
            contentContainer.addStyleName(css.contentVisible());
        } else {
            contentContainer.removeStyleName(css.contentVisible());
        }
    }

    private void handleEvents() {
        KeyDownHandler handler = new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (KeyboardEvent.KeyCode.ESC == event.getNativeEvent().getKeyCode()) {
                    event.stopPropagation();
                    event.preventDefault();
                    if (delegate != null) {
                        delegate.onEscapeKey();
                    }
                } else if (KeyboardEvent.KeyCode.ENTER == event.getNativeEvent().getKeyCode()) {
                    event.stopPropagation();
                    event.preventDefault();
                    if (delegate != null) {
                        delegate.onEnterKey();
                    }
                }
            }
        };

        focusPanel.addDomHandler(handler, KeyDownEvent.getType());

        closeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (delegate != null) {
                    delegate.onClose();
                }
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        /* Don't start moving the window when clicking close button */
        closeButton.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                event.preventDefault();
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        MouseHandler mouseHandler = new MouseHandler();
        header.addDomHandler(mouseHandler, MouseDownEvent.getType());
        header.addDomHandler(mouseHandler, MouseUpEvent.getType());
        header.addDomHandler(mouseHandler, MouseMoveEvent.getType());
    }

    public void setDelegate(Window.ViewEvents delegate) {
        this.delegate = delegate;
    }

    public void addContentWidget(Widget content) {
        this.content.add(content);
    }

    public Widget getContent() {
        return this.content;
    }

    private void endDragging(MouseUpEvent event) {
        dragging = false;
        DOM.releaseCapture(header.getElement());
        elemental.dom.Element element = (elemental.dom.Element)contentContainer.getElement();
        element.getStyle().setProperty("transition", transition);
    }

    private void continueDragging(MouseMoveEvent event) {
        if (dragging) {
            int absX = event.getX() + contentContainer.getAbsoluteLeft();
            int absY = event.getY() + contentContainer.getAbsoluteTop();

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
            DOM.setCapture(header.getElement());
            if ("".equals(contentContainer.getElement().getStyle().getPosition())) {
                contentContainer.getElement().getStyle().setTop(contentContainer.getAbsoluteTop() + 1, Style.Unit.PX);
                contentContainer.getElement().getStyle().setLeft(contentContainer.getAbsoluteLeft(), Style.Unit.PX);
            } else {
                contentContainer.getElement().getStyle().setTop(contentContainer.getAbsoluteTop(), Style.Unit.PX);
                contentContainer.getElement().getStyle().setLeft(contentContainer.getAbsoluteLeft(), Style.Unit.PX);

            }

            contentContainer.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            elemental.dom.Element element = (elemental.dom.Element)contentContainer.getElement();
            transition = element.getStyle().getPropertyValue("transition");
            element.getStyle().setProperty("transition", "all 0ms");

            dragStartX = event.getX();
            dragStartY = event.getY();
        }

    }

    /**
     * Sets the popup's position relative to the browser's client area. The
     * popup's position may be set before calling {@link #setShowing(boolean)}.
     *
     * @param left
     *         the left position, in pixels
     * @param top
     *         the top position, in pixels
     */
    public void setPopupPosition(int left, int top) {
        // Account for the difference between absolute position and the
        // body's positioning context.
        left -= Document.get().getBodyOffsetLeft();
        top -= Document.get().getBodyOffsetTop();

        // Set the popup's position manually, allowing setPopupPosition() to be
        // called before show() is called (so a popup can be positioned without it
        // 'jumping' on the screen).
        Element elem = contentContainer.getElement();
        elem.getStyle().setPropertyPx("left", left);
        elem.getStyle().setPropertyPx("top", top);
    }

    @UiTemplate("View.ui.xml")
    interface MyBinder extends UiBinder<FlowPanel, View> {
    }

    private class MouseHandler implements MouseDownHandler, MouseUpHandler,
                                          MouseMoveHandler {

        public void onMouseDown(MouseDownEvent event) {
            beginDragging(event);
        }

        public void onMouseMove(MouseMoveEvent event) {
            continueDragging(event);
        }

        public void onMouseUp(MouseUpEvent event) {
            endDragging(event);

            if (lastFocused != null) {
                lastFocused.setFocus(true);
            }
        }
    }

    /**
     * Sets focus on the last focused child element if such exists.
     */
    public void focusLastFocusedElement() {
        if (lastFocused != null) {
            lastFocused.setFocus(true);
        }
    }

    /**
     * Sets focus on the first child of content panel if such exists.
     * Otherwise sets focus on first child of footer
     */
    public void setFocus() {
        if (!setFocusOnChildOf(content)) {
            setFocusOnChildOf(footer);
        }
    }

    /**
     * Sets focus on the first focusable child if such exists.
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

    public native boolean isElementFocused(Element element) /*-{
        return $doc.activeElement == element;
    }-*/;
}
