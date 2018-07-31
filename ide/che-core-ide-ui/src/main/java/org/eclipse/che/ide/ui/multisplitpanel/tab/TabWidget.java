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
package org.eclipse.che.ide.ui.multisplitpanel.tab;

import static com.google.gwt.dom.client.NativeEvent.BUTTON_LEFT;
import static com.google.gwt.dom.client.NativeEvent.BUTTON_MIDDLE;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Widget that represents a tab.
 *
 * @author Artem Zatsarynnyi
 */
public class TabWidget extends Composite implements Tab {

  private static final TabItemWidgetUiBinder UI_BINDER = GWT.create(TabItemWidgetUiBinder.class);

  private final String title;
  private final SVGResource icon;

  @UiField SimplePanel iconPanel;

  @UiField Label titleLabel;

  @UiField FlowPanel closeButton;

  @UiField(provided = true)
  PartStackUIResources resources;

  private ActionDelegate delegate;

  @Inject
  public TabWidget(
      PartStackUIResources resources,
      @Assisted String title,
      @Assisted SVGResource icon,
      @Assisted boolean closable) {
    this.resources = resources;
    this.title = title;
    this.icon = MoreObjects.firstNonNull(icon, emptySVGResource());

    initWidget(UI_BINDER.createAndBindUi(this));

    titleLabel.setText(title);

    iconPanel.add(new SVGImage(getIcon()));

    addDomHandler(this, ClickEvent.getType());
    addDomHandler(this, DoubleClickEvent.getType());

    if (closable) {
      closeButton.addDomHandler(
          event -> delegate.onTabClosing(TabWidget.this), ClickEvent.getType());
    } else {
      closeButton.setVisible(false);
    }
  }

  @Override
  public SVGResource getIcon() {
    return icon;
  }

  @Override
  public String getTitleText() {
    return title;
  }

  @Override
  public void select() {
    getElement().setAttribute("focused", "");
  }

  @Override
  public void unSelect() {
    getElement().removeAttribute("focused");
  }

  @Override
  public void onClick(@NotNull ClickEvent event) {
    if (BUTTON_LEFT == event.getNativeButton()) {
      delegate.onTabClicked(this);
    } else if (BUTTON_MIDDLE == event.getNativeButton()) {
      delegate.onTabClosing(this);
    }
  }

  @Override
  public void onDoubleClick(DoubleClickEvent event) {
    delegate.onTabDoubleClicked(this);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  interface TabItemWidgetUiBinder extends UiBinder<Widget, TabWidget> {}

  private static SVGResource emptySVGResource() {
    return new SVGResource() {
      @Override
      public OMSVGSVGElement getSvg() {
        return new OMSVGSVGElement();
      }

      @Override
      public SafeUri getSafeUri() {
        return null;
      }

      @Override
      public String getUrl() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }
    };
  }
}
