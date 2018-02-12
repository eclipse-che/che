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
package org.eclipse.che.ide.editor.orion.client.signature;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.eclipse.che.ide.api.theme.Style.theme;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.CustomEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventTarget;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.HTMLCollection;
import elemental.html.SpanElement;
import elemental.html.Window;
import java.util.List;
import org.eclipse.che.ide.api.editor.signature.ParameterInfo;
import org.eclipse.che.ide.api.editor.signature.SignatureHelp;
import org.eclipse.che.ide.api.editor.signature.SignatureInfo;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * The widget for representing the structure of method's signatures and navigate on it.
 *
 * @author Valeriy Svydenko
 */
public class SignatureWidget implements EventListener {
  private static final String DOCUMENTATION = "documentation";
  private static final int DOM_ITEMS_SIZE = 50;

  private final PopupResources popupResources;
  /** The related editor. */
  private final OrionEditorWidget textEditor;
  /** The main element for the popup. */
  private final Element popupElement;

  private final Element popupBodyElement;
  /** The list (ul) element for the popup. */
  private final Element listElement;

  private final EventListener popupListener;
  private final OrionKeyModeOverlay assistMode;

  private boolean visible = false;
  private boolean focused = false;

  /** The previously focused element. */
  private Element selectedElement;

  private FlowPanel docPopup;

  private SignatureHelp signatureHelp;
  private Timer showDocTimer =
      new Timer() {
        @Override
        public void run() {
          if (selectedElement != null) {
            selectedElement.dispatchEvent(createValidateEvent(DOCUMENTATION));
          }
        }
      };

  @AssistedInject
  public SignatureWidget(
      final PopupResources popupResources,
      @Assisted final OrionEditorWidget textEditor,
      @Assisted final OrionKeyModeOverlay assistMode) {
    this.popupResources = popupResources;
    this.textEditor = textEditor;
    this.assistMode = assistMode;

    popupElement = Elements.createDivElement(popupResources.popupStyle().popup());

    Element headerElement = Elements.createDivElement(popupResources.popupStyle().header());
    headerElement.setInnerText("Signatures:");
    popupElement.appendChild(headerElement);

    popupBodyElement = Elements.createDivElement(popupResources.popupStyle().body());
    popupElement.appendChild(popupBodyElement);

    listElement = Elements.createUListElement();
    popupBodyElement.appendChild(listElement);

    docPopup = new FlowPanel();
    docPopup.setStyleName(popupResources.popupStyle().popup());
    docPopup.setSize("370px", "180px");

    popupListener =
        evt -> {
          if (!(evt instanceof MouseEvent)) {
            return;
          }
          final MouseEvent mouseEvent = (MouseEvent) evt;
          final EventTarget target = mouseEvent.getTarget();
          if (target instanceof Element) {
            final Element elementTarget = (Element) target;
            if (docPopup.isVisible()
                && (elementTarget.equals(docPopup.getElement())
                    || elementTarget.getParentElement().equals(docPopup.getElement()))) {
              return;
            }

            if (!SignatureWidget.this.popupElement.contains(elementTarget)) {
              hide();
              evt.preventDefault();
            }
          }
        };
  }

  private native CustomEvent createValidateEvent(String eventType) /*-{
    return new CustomEvent(eventType);
  }-*/;

  private Element createSignaturesPopupItem(int index) {
    List<SignatureInfo> signatures = signatureHelp.getSignatures();
    SignatureInfo signature = signatures.get(index);
    Element element = Elements.createLiElement(popupResources.popupStyle().item());
    element.setId(Integer.toString(index));

    SpanElement label = Elements.createSpanElement(popupResources.popupStyle().label());
    renderSignature(label, signature, signatureHelp.getActiveParameter());
    element.appendChild(label);
    element.setTabIndex(1);

    element.addEventListener(Event.CLICK, event -> select(element), false);
    element.addEventListener(Event.FOCUS, this, false);

    element.addEventListener(
        DOCUMENTATION,
        event -> {
          Optional<String> docOptional = signature.getDocumentation();
          String documentation = docOptional.isPresent() ? docOptional.get() : "";
          Widget info = createAdditionalInfoWidget(documentation);
          if (info != null) {
            docPopup.clear();
            docPopup.add(info);
            docPopup.getElement().getStyle().setOpacity(1);

            if (!docPopup.isAttached()) {
              final int x = popupElement.getOffsetLeft() + popupElement.getOffsetWidth() + 3;
              final int y = popupElement.getOffsetTop();
              RootPanel.get().add(docPopup);
              updateMenuPosition(docPopup, x, y);
            }
          } else {
            docPopup.getElement().getStyle().setOpacity(0);
          }
        },
        false);

    return element;
  }

  private Widget createAdditionalInfoWidget(String documentation) {
    if (documentation == null || documentation.trim().isEmpty()) {
      documentation = "No documentation found.";
    }

    HTML widget = new HTML(documentation);
    widget.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_LINE);
    widget.getElement().getStyle().setColor(theme.completionPopupItemTextColor());
    widget.getElement().getStyle().setFontSize(13, Style.Unit.PX);
    widget.getElement().getStyle().setMarginLeft(4, Style.Unit.PX);
    widget.getElement().getStyle().setOverflow(Overflow.AUTO);
    widget.getElement().getStyle().setProperty("userSelect", "text");
    widget.setHeight("100%");
    return widget;
  }

  private void renderSignature(
      Element signature, SignatureInfo signatureInfo, Optional<Integer> activeParameter) {
    Element code = Elements.createDivElement();
    signature.appendChild(code);
    boolean hasParameters =
        signatureInfo.getParameters().isPresent() && !signatureInfo.getParameters().get().isEmpty();

    if (hasParameters) {
      renderParameters(code, signatureInfo, activeParameter);
    } else {
      Node label = code.appendChild(Elements.createSpanElement());
      label.setTextContent(signatureInfo.getLabel());
    }
  }

  private void renderParameters(
      Element parent, SignatureInfo signatureInfo, Optional<Integer> activeParameter) {
    int end = signatureInfo.getLabel().length();
    int idx;
    Element element;
    for (int i = signatureInfo.getParameters().get().size() - 1; i >= 0; i--) {
      ParameterInfo parameterInfo = signatureInfo.getParameters().get().get(i);
      idx = signatureInfo.getLabel().lastIndexOf(parameterInfo.getLabel(), end);
      int signatureLabelOffset = 0;
      int signatureLabelEnd = 0;
      if (idx >= 0) {
        signatureLabelOffset = idx;
        signatureLabelEnd = idx + parameterInfo.getLabel().length();
      }

      element = Elements.createSpanElement();
      element.setTextContent(signatureInfo.getLabel().substring(signatureLabelEnd, end));
      parent.insertBefore(element, parent.getFirstElementChild());

      element = Elements.createSpanElement(popupResources.popupStyle().parameter());
      if (activeParameter.isPresent() && i == activeParameter.get()) {
        Elements.addClassName(popupResources.popupStyle().active(), element);
      }
      element.setTextContent(
          signatureInfo.getLabel().substring(signatureLabelOffset, signatureLabelEnd));
      parent.insertBefore(element, parent.getFirstElementChild());
      end = signatureLabelOffset;
    }
    element = Elements.createSpanElement();
    element.setTextContent(signatureInfo.getLabel().substring(0, end));
    parent.insertBefore(element, parent.getFirstElementChild());
  }

  private void updateMenuPosition(FlowPanel popupMenu, int x, int y) {
    if (x + popupMenu.getOffsetWidth() > com.google.gwt.user.client.Window.getClientWidth()) {
      popupMenu
          .getElement()
          .getStyle()
          .setLeft(
              x - popupMenu.getOffsetWidth() - popupElement.getOffsetWidth() - 5, Style.Unit.PX);
    } else {
      popupMenu.getElement().getStyle().setLeft(x, Style.Unit.PX);
    }

    if (y + popupMenu.getOffsetHeight() > com.google.gwt.user.client.Window.getClientHeight()) {
      popupMenu
          .getElement()
          .getStyle()
          .setTop(
              y - popupMenu.getOffsetHeight() - popupElement.getOffsetHeight() - 3, Style.Unit.PX);
    } else {
      popupMenu.getElement().getStyle().setTop(y, Style.Unit.PX);
    }
  }

  private void addPopupEventListeners() {
    Elements.getDocument().addEventListener(Event.MOUSEDOWN, this.popupListener, false);

    textEditor.getTextView().addKeyMode(assistMode);

    // add key event listener on popup
    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistCancel",
            () -> {
              hide();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistPreviousProposal",
            () -> {
              selectPrevious();
              return true;
            });

    textEditor
        .getTextView()
        .setAction(
            "cheContentAssistNextProposal",
            () -> {
              selectNext();
              return true;
            });

    listElement.addEventListener(Event.KEYDOWN, this, false);
    popupBodyElement.addEventListener(Event.SCROLL, this, false);
  }

  private void removePopupEventListeners() {
    textEditor.getTextView().removeKeyMode(assistMode);
    listElement.removeEventListener(Event.KEYDOWN, this, false);
    popupBodyElement.removeEventListener(Event.SCROLL, this, false);
    Elements.getDocument().removeEventListener(Event.MOUSEDOWN, this.popupListener);
  }

  private void selectFirst() {
    scrollTo(0);
    updateIfNecessary();
    select(getFirstItemInDOM());
  }

  private void selectLast() {
    scrollTo(getTotalItems() - 1);
    updateIfNecessary();
    select(getLastItemInDOM());
  }

  private void select(int index) {
    select(getItem(index));
  }

  private void selectPrevious() {
    Element previousElement = selectedElement.getPreviousElementSibling();
    if (previousElement != null
        && previousElement == getExtraTopRow()
        && getExtraTopRow().isHidden()) {
      selectLast();
    } else {
      selectOffset(-1);
    }
  }

  private void selectNext() {
    Element nextElement = selectedElement.getNextElementSibling();
    if (nextElement != null
        && nextElement == getExtraBottomRow()
        && getExtraBottomRow().isHidden()) {
      selectFirst();
    } else {
      selectOffset(1);
    }
  }

  private void selectOffset(int offset) {
    int index = getItemId(selectedElement) + offset;
    index = Math.max(index, 0);
    index = Math.min(index, getTotalItems() - 1);

    if (!isItemInDOM(index)) {
      scrollTo(index);
      update();
    }

    select(index);
  }

  private void select(Element element) {
    if (element == selectedElement) {
      return;
    }

    if (selectedElement != null) {
      selectedElement.removeAttribute("selected");
    }
    selectedElement = element;
    selectedElement.setAttribute("selected", "true");

    showDocTimer.cancel();
    showDocTimer.schedule(docPopup.isAttached() ? 100 : 1500);

    if (selectedElement.getOffsetTop() < this.popupBodyElement.getScrollTop()) {
      selectedElement.scrollIntoView(true);
    } else if ((selectedElement.getOffsetTop() + selectedElement.getOffsetHeight())
        > (this.popupBodyElement.getScrollTop() + this.popupBodyElement.getClientHeight())) {
      selectedElement.scrollIntoView(false);
    }
  }

  /**
   * Displays assist popup relative to the current cursor position.
   *
   * @param signatureHelp signatures to display
   */
  public void show(SignatureHelp signatureHelp) {
    this.signatureHelp = signatureHelp;

    OrionTextViewOverlay textView = textEditor.getTextView();
    OrionPixelPositionOverlay caretLocation =
        textView.getLocationAtOffset(textView.getCaretOffset());
    caretLocation.setY(caretLocation.getY() + textView.getLineHeight());
    caretLocation = textView.convert(caretLocation, "document", "page");

    listElement.setInnerHTML("");

    popupElement.getStyle().setLeft(caretLocation.getX(), PX);
    popupElement.getStyle().setTop(caretLocation.getY(), PX);
    popupElement.getStyle().setWidth("400px");
    popupElement.getStyle().setHeight("100px");
    popupElement.getStyle().setOpacity(1);
    Elements.getDocument().getBody().appendChild(this.popupElement);

    /* Add the top extra row. */
    setExtraRowHeight(appendExtraRow(), 0);

    /* Add the popup items. */
    for (int i = 0; i < Math.min(DOM_ITEMS_SIZE, getTotalItems()); i++) {
      listElement.appendChild(createSignaturesPopupItem(i));
    }

    /* Add the bottom extra row. */
    setExtraRowHeight(appendExtraRow(), Math.max(0, getTotalItems() - DOM_ITEMS_SIZE));

    /* Correct popup position (wants to be refactored) */
    final Window window = Elements.getWindow();
    final int viewportWidth = window.getInnerWidth();
    final int viewportHeight = window.getInnerHeight();

    int spaceBelow = viewportHeight - caretLocation.getY();
    if (this.popupElement.getOffsetHeight() > spaceBelow) {
      // Check if div is too large to fit above
      int spaceAbove = caretLocation.getY() - textView.getLineHeight();
      if (this.popupElement.getOffsetHeight() > spaceAbove) {
        // Squeeze the div into the larger area
        if (spaceBelow > spaceAbove) {
          this.popupElement.getStyle().setProperty("maxHeight", spaceBelow + "px");
        } else {
          this.popupElement.getStyle().setProperty("maxHeight", spaceAbove + "px");
          this.popupElement.getStyle().setTop("0");
        }
      } else {
        // Put the div above the line
        this.popupElement
            .getStyle()
            .setTop(
                (caretLocation.getY()
                        - this.popupElement.getOffsetHeight()
                        - textView.getLineHeight())
                    + "px");
        this.popupElement.getStyle().setProperty("maxHeight", spaceAbove + "px");
      }
    } else {
      this.popupElement.getStyle().setProperty("maxHeight", spaceBelow + "px");
    }

    if (caretLocation.getX() + this.popupElement.getOffsetWidth() > viewportWidth) {
      int leftSide = viewportWidth - this.popupElement.getOffsetWidth();
      if (leftSide < 0) {
        leftSide = 0;
      }
      this.popupElement.getStyle().setLeft(leftSide + "px");
      this.popupElement.getStyle().setProperty("maxWidth", (viewportWidth - leftSide) + "px");
    } else {
      this.popupElement
          .getStyle()
          .setProperty("maxWidth", viewportWidth + caretLocation.getX() + "px");
    }

    /* Don't attach handlers twice. Visible popup must already have their attached. */
    if (!visible) {
      addPopupEventListeners();
    }

    visible = true;
    focused = false;

    /* Update documentation popup position */
    docPopup
        .getElement()
        .getStyle()
        .setLeft(popupElement.getOffsetLeft() + popupElement.getOffsetWidth() + 3, Style.Unit.PX);
    docPopup.getElement().getStyle().setTop(popupElement.getOffsetTop(), Style.Unit.PX);

    /* Select first row. */
    Optional<Integer> activeSignature = signatureHelp.getActiveSignature();
    if (!activeSignature.isPresent()) {
      selectFirst();
    } else {
      Integer index = activeSignature.get();
      select(index);
    }
  }

  /** Hides the popup and displaying javadoc. */
  public void hide() {
    textEditor.setFocus();

    if (docPopup.isAttached()) {
      docPopup.getElement().getStyle().setOpacity(0);
      new Timer() {
        @Override
        public void run() {
          docPopup.removeFromParent();
        }
      }.schedule(250);
    }

    popupElement.getStyle().setOpacity(0);
    new Timer() {
      @Override
      public void run() {
        // detach assist popup
        if (popupElement.getParentNode() != null) {
          popupElement.getParentNode().removeChild(popupElement);
        }
        // remove all items from popup element
        listElement.setInnerHTML("");
      }
    }.schedule(250);

    visible = false;
    selectedElement = null;
    showDocTimer.cancel();

    removePopupEventListeners();
  }

  @Override
  public void handleEvent(Event evt) {
    if (Event.KEYDOWN.equalsIgnoreCase(evt.getType())) {
      final KeyboardEvent keyEvent = (KeyboardEvent) evt;
      switch (keyEvent.getKeyCode()) {
        case KeyCodes.KEY_ESCAPE:
          Scheduler.get().scheduleDeferred(this::hide);
          break;

        case KeyCodes.KEY_DOWN:
          selectNext();
          evt.preventDefault();
          break;

        case KeyCodes.KEY_UP:
          selectPrevious();
          evt.preventDefault();
          break;
      }
    } else if (Event.SCROLL.equalsIgnoreCase(evt.getType())) {
      updateIfNecessary();
    } else if (Event.FOCUS.equalsIgnoreCase(evt.getType())) {
      focused = true;
    }
  }

  private void updateIfNecessary() {
    int scrollTop = popupBodyElement.getScrollTop();
    int extraTopHeight = getExtraTopRow().getClientHeight();

    if (scrollTop < extraTopHeight) {
      // the scroll bar is above the buffered area
      update();
    } else if (scrollTop + popupBodyElement.getClientHeight()
        > extraTopHeight + getItemHeight() * DOM_ITEMS_SIZE) {
      // the scroll bar is below the buffered area
      update();
    }
  }

  private void update() {
    int scrollTop = popupBodyElement.getScrollTop();
    int itemHeight = getItemHeight();
    int topVisibleItem = scrollTop == 0 || itemHeight == 0 ? 0 : scrollTop / itemHeight;
    int topDOMItem = Math.max(0, topVisibleItem - (DOM_ITEMS_SIZE - getItemsPerPage()) / 2);
    int bottomDOMItem = Math.min(getTotalItems() - 1, topDOMItem + DOM_ITEMS_SIZE - 1);
    if (bottomDOMItem == getTotalItems() - 1) {
      topDOMItem = Math.max(0, bottomDOMItem - DOM_ITEMS_SIZE + 1);
    }

    // resize the extra top row
    setExtraRowHeight(getExtraTopRow(), topDOMItem);

    // replace the DOM items with new content based on the scroll position
    HTMLCollection nodes = listElement.getChildren();
    for (int i = 0; i <= (bottomDOMItem - topDOMItem); i++) {
      Element newNode = createSignaturesPopupItem(topDOMItem + i);
      listElement.replaceChild(newNode, nodes.item(i + 1));

      // check if the item is the selected
      if (newNode.getId().equals(selectedElement.getId())) {
        selectedElement = newNode;
        selectedElement.setAttribute("selected", "true");
      }
    }

    // resize the extra bottom row
    setExtraRowHeight(getExtraBottomRow(), getTotalItems() - (bottomDOMItem + 1));

    // ensure the keyboard focus is in the visible area
    if (focused) {
      getItem(topDOMItem + (bottomDOMItem - topDOMItem) / 2).focus();
    }
  }

  /**
   * Check visibility of the widget.
   *
   * @return <b>true</b> if the popup is visible, otherwise returns <b>false</b>
   */
  public boolean isVisible() {
    return visible;
  }

  private Element getExtraTopRow() {
    return (listElement == null) ? null : listElement.getFirstElementChild();
  }

  private Element getExtraBottomRow() {
    return (listElement == null) ? null : listElement.getLastElementChild();
  }

  private Element getFirstItemInDOM() {
    Element extraTopRow = getExtraTopRow();
    return (extraTopRow == null) ? null : extraTopRow.getNextElementSibling();
  }

  private Element getLastItemInDOM() {
    Element extraBottomRow = getExtraBottomRow();
    return (extraBottomRow == null) ? null : extraBottomRow.getPreviousElementSibling();
  }

  private int getItemId(Element item) {
    return Integer.parseInt(item.getId());
  }

  private Element getItem(int index) {
    return (Element) listElement.getChildren().namedItem(Integer.toString(index));
  }

  private int getItemHeight() {
    Element item = getFirstItemInDOM();
    return (item == null) ? 0 : item.getClientHeight();
  }

  private Element appendExtraRow() {
    Element extraRow = Elements.createLiElement();
    listElement.appendChild(extraRow);
    return extraRow;
  }

  private void setExtraRowHeight(Element extraRow, int items) {
    int height = items * getItemHeight();
    extraRow.getStyle().setHeight(height + "px");
    extraRow.setHidden(height <= 0);
  }

  private int getItemsPerPage() {
    int itemHeight = getItemHeight();
    if (itemHeight == 0) {
      return 0;
    }
    return (int) Math.ceil((double) popupBodyElement.getClientHeight() / itemHeight);
  }

  private int getTotalItems() {
    List<SignatureInfo> signatures = signatureHelp.getSignatures();
    return (signatures == null) ? 0 : signatures.size();
  }

  private boolean isItemInDOM(int index) {
    return index >= getItemId(getFirstItemInDOM()) && index <= getItemId(getLastItemInDOM());
  }

  private void scrollTo(int index) {
    int currentScrollTop = popupBodyElement.getScrollTop();
    int newScrollTop = index * getItemHeight();
    if (currentScrollTop < newScrollTop) {
      newScrollTop -= popupBodyElement.getClientHeight();
    }
    popupBodyElement.setScrollTop(newScrollTop);
  }
}
