/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.multisplitpanel.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.ui.multisplitpanel.tab.Tab;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Implementation of {@link MenuItem} that represents {@link Tab}.
 *
 * @author Artem Zatsarynnyi
 */
public class MenuItemWidget extends Composite implements MenuItem<Tab> {

  private static final MenuItemWidgetUiBinder UI_BINDER = GWT.create(MenuItemWidgetUiBinder.class);

  @UiField FlowPanel iconPanel;
  @UiField Label title;
  @UiField FlowPanel closeButton;

  private Tab tab;
  private ActionDelegate delegate;

  public MenuItemWidget(Tab tab, boolean closable) {
    initWidget(UI_BINDER.createAndBindUi(this));
    this.tab = tab;

    Widget icon = new SVGImage(tab.getIcon());
    iconPanel.add(icon);
    title.setText(tab.getTitleText());

    addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (delegate != null) {
              delegate.onItemSelected(MenuItemWidget.this);
            }
          }
        },
        ClickEvent.getType());

    if (closable) {
      closeButton.addDomHandler(
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
              clickEvent.stopPropagation();
              clickEvent.preventDefault();

              if (delegate != null) {
                delegate.onItemClosing(MenuItemWidget.this);
              }
            }
          },
          ClickEvent.getType());
    } else {
      closeButton.setVisible(false);
    }
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Tab getData() {
    return tab;
  }

  interface MenuItemWidgetUiBinder extends UiBinder<Widget, MenuItemWidget> {}
}
