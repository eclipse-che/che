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
package org.eclipse.che.ide.part.widgets.panemenu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.EditorPartStack;

/**
 * Menu for managing opened editors for corresponding {@link EditorPartStack}.
 *
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 * @author Roman Nikitenko
 */
public class EditorPaneMenuWidget extends Composite implements EditorPaneMenu {

  interface EditorPaneMenuWidgetUiBinder extends UiBinder<Widget, EditorPaneMenuWidget> {}

  private static final String GWT_POPUP_STANDARD_STYLE = "gwt-PopupPanel";

  private static final EditorPaneMenuWidgetUiBinder UI_BINDER =
      GWT.create(EditorPaneMenuWidgetUiBinder.class);

  private final PopupPanel popupPanel;

  private final FlowPanel actionsPanel;
  private final FlowPanel itemsPanel;

  @UiField(provided = true)
  final Resources resources;

  private long closeTime;

  @Inject
  public EditorPaneMenuWidget(Resources resources) {
    this.resources = resources;

    initWidget(UI_BINDER.createAndBindUi(this));

    closeTime = System.currentTimeMillis();
    addDomHandler(
        new MouseDownHandler() {
          @Override
          public void onMouseDown(MouseDownEvent event) {
            long time = System.currentTimeMillis();
            if (time - closeTime < 100) {
              return;
            }
            show();
          }
        },
        MouseDownEvent.getType());

    FlowPanel rootPanel = new FlowPanel();
    rootPanel.addStyleName(resources.partStackCss().listItemPanel());

    itemsPanel = new FlowPanel();
    actionsPanel = new FlowPanel();

    popupPanel = new PopupPanel();
    popupPanel.setAutoHideEnabled(true);
    popupPanel.removeStyleName(GWT_POPUP_STANDARD_STYLE);

    popupPanel.add(rootPanel);
    rootPanel.add(itemsPanel);
    rootPanel.add(getDelimiter());
    rootPanel.add(actionsPanel);

    popupPanel.addCloseHandler(
        new CloseHandler<PopupPanel>() {
          @Override
          public void onClose(CloseEvent<PopupPanel> event) {
            closeTime = System.currentTimeMillis();
          }
        });
  }

  @Override
  public void show() {
    int x = getAbsoluteLeft() + getOffsetWidth() - 6;
    int y = getAbsoluteTop() + 19;

    popupPanel.show();
    popupPanel.getElement().getStyle().setProperty("position", "absolute");
    popupPanel.getElement().getStyle().clearProperty("left");
    popupPanel.getElement().getStyle().setProperty("right", "calc(100% - " + x + "px");
    popupPanel.getElement().getStyle().setProperty("top", "" + y + "px");
  }

  @Override
  public void hide() {
    popupPanel.hide();
  }

  @Override
  public void addItem(@NotNull EditorPaneMenuItem item) {
    addItem(item, false);
  }

  /** {@inheritDoc} */
  @Override
  public void addItem(@NotNull EditorPaneMenuItem item, boolean isSeparated) {
    FlowPanel targetPanel = item instanceof PaneMenuActionItemWidget ? actionsPanel : itemsPanel;
    targetPanel.add(item);

    if (isSeparated) {
      targetPanel.add(getDelimiter());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void removeItem(@NotNull EditorPaneMenuItem item) {
    FlowPanel targetPanel = item instanceof PaneMenuActionItemWidget ? actionsPanel : itemsPanel;
    targetPanel.remove(item);
  }

  private FlowPanel getDelimiter() {
    final FlowPanel delimiter = new FlowPanel();
    delimiter.addStyleName(resources.coreCss().editorPaneMenuDelimiter());
    return delimiter;
  }
}
