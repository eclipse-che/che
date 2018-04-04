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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * Implementation of {@link EditorPaneMenuItem} to displaying editor's actions in {@link
 * EditorPaneMenu}
 *
 * @author Roman Nikitenko
 */
public class PaneMenuActionItemWidget extends Composite implements EditorPaneMenuItem<Action> {

  interface PaneMenuActionItemWidgetUiBinder extends UiBinder<Widget, PaneMenuActionItemWidget> {}

  private static final PaneMenuActionItemWidgetUiBinder UI_BINDER =
      GWT.create(PaneMenuActionItemWidgetUiBinder.class);

  private Action action;

  @UiField FlowPanel iconPanel;

  @UiField Label title;

  private ActionDelegate<Action> delegate;

  public PaneMenuActionItemWidget(@NotNull Action action) {
    initWidget(UI_BINDER.createAndBindUi(this));
    this.action = action;
    Presentation presentation = action.getTemplatePresentation();
    title.setText(presentation.getText());

    addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (delegate != null) {
              delegate.onItemClicked(PaneMenuActionItemWidget.this);
            }
          }
        },
        ClickEvent.getType());
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate<Action> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Action getData() {
    return action;
  }
}
