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
package org.eclipse.che.ide.command.toolbar.selector;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Implementation for {PanelSelectorView}. */
@Singleton
public class PanelSelectorViewImpl extends Composite implements PanelSelectorView {

  interface PanelSelectorViewImplUiBinder extends UiBinder<Widget, PanelSelectorViewImpl> {}

  private ActionDelegate delegate;

  private Resources resources;

  @UiField FlowPanel button;

  @UiField FlowPanel popup;

  @UiField FlowPanel selectorLeft;

  @UiField FlowPanel selectorLeftBottom;

  @UiField FlowPanel selectorFullEditor;

  @UiField FlowPanel selectorBottom;

  @UiField FlowPanel selectorRight;

  @UiField FlowPanel selectorLeftRightBottom;

  @UiField FlowPanel iconPanel;

  private EventRemover clickListenerRemover;

  @Inject
  public PanelSelectorViewImpl(PanelSelectorViewImplUiBinder uiBinder, Resources resources) {
    this.resources = resources;
    initWidget(uiBinder.createAndBindUi(this));

    button.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            delegate.onButtonClicked();
          }
        },
        ClickEvent.getType());
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setState(State state) {
    switch (state) {
      case LEFT:
        setIcon(resources.panelSelectorLeft());
        break;

      case LEFT_BOTTOM:
        setIcon(resources.panelSelectorLeftBottom());
        break;

      case FULL_EDITOR:
        setIcon(resources.panelSelectorFullEditor());
        break;

      case BOTTOM:
        setIcon(resources.panelSelectorBottom());
        break;

      case RIGHT:
        setIcon(resources.panelSelectorRight());
        break;

      case LEFT_RIGHT_BOTTOM:
        setIcon(resources.panelSelectorLeftRightBottom());
        break;
    }
  }

  private void setIcon(SVGResource resource) {
    iconPanel.clear();
    SVGImage image = new SVGImage(resource);
    iconPanel.add(image);
  }

  @Override
  public void showPopup() {
    if ("block".equals(popup.getElement().getStyle().getProperty("display"))) {
      hidePopup();
      return;
    }

    popup.getElement().getStyle().setProperty("display", "block");
    clickListenerRemover =
        Elements.getBody().addEventListener(Event.MOUSEDOWN, clickListener, true);
  }

  private final EventListener clickListener =
      new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          if (((Element) popup.getElement()).contains((Node) evt.getTarget())) {

            if (((Element) selectorLeft.getElement()).contains((Node) evt.getTarget())) {
              delegate.onSelectorLeftClicked();
            } else if (((Element) selectorLeftBottom.getElement())
                .contains((Node) evt.getTarget())) {
              delegate.onSelectorLeftBottomClicked();
            } else if (((Element) selectorFullEditor.getElement())
                .contains((Node) evt.getTarget())) {
              delegate.onSelectorFullEditorClicked();
            } else if (((Element) selectorBottom.getElement()).contains((Node) evt.getTarget())) {
              delegate.onSelectorBottomClicked();
            } else if (((Element) selectorRight.getElement()).contains((Node) evt.getTarget())) {
              delegate.onSelectorRightClicked();
            } else if (((Element) selectorLeftRightBottom.getElement())
                .contains((Node) evt.getTarget())) {
              delegate.onSelectorLeftRightBottomClicked();
            } else {
              return;
            }

            hidePopup();
            return;
          }

          if (((Element) button.getElement()).contains((Node) evt.getTarget())) {
            return;
          }

          hidePopup();
        }
      };

  @Override
  public void hidePopup() {
    popup.getElement().getStyle().clearProperty("display");

    if (clickListenerRemover != null) {
      clickListenerRemover.remove();
      clickListenerRemover = null;
    }
  }
}
