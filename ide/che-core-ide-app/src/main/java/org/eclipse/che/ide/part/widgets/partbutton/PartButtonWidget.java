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
package org.eclipse.che.ide.part.widgets.partbutton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Widget that response for displaying part tab.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 * @author Vitaliy Guliy
 * @author Vlad Zhukovskyi
 */
public class PartButtonWidget extends Composite implements PartButton {

  private static final PartButtonWidgetUiBinder UI_BINDER =
      GWT.create(PartButtonWidgetUiBinder.class);

  @UiField FlowPanel iconPanel;

  private ActionDelegate delegate;

  private Widget badgeWidget;

  private SVGResource tabIcon;

  @Inject
  public PartButtonWidget(@Assisted String title) {
    initWidget(UI_BINDER.createAndBindUi(this));
    ensureDebugId("partButton-" + title);

    addDomHandler(this, DoubleClickEvent.getType());
    addDomHandler(this, ClickEvent.getType());
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public IsWidget getView() {
    return asWidget();
  }

  @Override
  public Widget getIcon() {
    return tabIcon != null ? new SVGImage(tabIcon) : null;
  }

  private Tooltip tooltipHint;

  /** {@inheritDoc} */
  @NotNull
  public PartButton setTooltip(@Nullable String tooltip) {
    if (tooltipHint == null) {
      tooltipHint =
          Tooltip.create(
              (elemental.dom.Element) getElement(),
              PositionController.VerticalAlign.BOTTOM,
              PositionController.HorizontalAlign.MIDDLE,
              tooltip);
    } else {
      tooltipHint.setTitle(tooltip);
    }

    return this;
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public PartButton setIcon(@Nullable SVGResource iconResource) {
    this.tabIcon = iconResource;
    iconPanel.clear();

    if (tabIcon != null) {
      iconPanel.add(new SVGImage(tabIcon));
    }

    return this;
  }

  /** {@inheritDoc} */
  @Override
  public void update(@NotNull PartPresenter part) {
    if (badgeWidget != null) {
      badgeWidget.getElement().removeFromParent();
      badgeWidget = null;
    }

    int unreadMessages = part.getUnreadNotificationsCount();
    if (unreadMessages == 0) {
      return;
    }

    badgeWidget = getBadge(unreadMessages);
    if (badgeWidget != null) {
      iconPanel.getParent().getElement().appendChild(badgeWidget.asWidget().getElement());
      updateBadge();
    }
  }

  /**
   * Creates a badge widget with a message
   *
   * @param messages messages count
   * @return new badge widget
   */
  private Widget getBadge(int messages) {
    FlowPanel w = new FlowPanel();
    Style s = w.getElement().getStyle();

    s.setProperty("position", "absolute");
    s.setProperty("width", "12px");
    s.setProperty("height", "12px");

    s.setProperty("boxSizing", "border-box");
    s.setProperty("borderRadius", "8px");
    s.setProperty("textAlign", "center");

    s.setProperty("color", org.eclipse.che.ide.api.theme.Style.getBadgeFontColor());

    s.setProperty("left", "9px");
    s.setProperty("top", "2px");

    s.setProperty("borderWidth", "1.5px");
    s.setProperty("borderStyle", "solid");

    s.setProperty(
        "fontFamily", "'Helvetica Neue', 'Myriad Pro', arial, Verdana, Verdana, sans-serif");
    s.setProperty("fontSize", "9.5px");
    s.setProperty("fontWeight", "bold");
    s.setProperty("textShadow", "none");

    s.setProperty("backgroundColor", org.eclipse.che.ide.api.theme.Style.getBadgeBackgroundColor());

    w.setStyleName("bounceOutUp");

    if (messages > 9) {
      s.setProperty("lineHeight", "5px");
      w.getElement().setInnerHTML("...");
    } else {
      s.setProperty("lineHeight", "10px");
      w.getElement().setInnerHTML("" + messages);
    }

    return w;
  }

  /** {@inheritDoc} */
  @Override
  public void onClick(@NotNull ClickEvent event) {
    delegate.onTabClicked(this);
  }

  @Override
  public void onDoubleClick(DoubleClickEvent event) {
    event.stopPropagation();
    event.preventDefault();
  }

  /** {@inheritDoc} */
  @Override
  public void select() {
    getElement().setAttribute("selected", "true");
    updateBadge();
  }

  /** {@inheritDoc} */
  @Override
  public void unSelect() {
    getElement().removeAttribute("selected");
    updateBadge();
  }

  /** Updates a badge style. */
  private void updateBadge() {
    if (badgeWidget == null) {
      return;
    }

    if (getElement().hasAttribute("selected")) {
      badgeWidget
          .getElement()
          .getStyle()
          .setBorderColor(org.eclipse.che.ide.api.theme.Style.theme.activeTabBackground());
    } else {
      badgeWidget
          .getElement()
          .getStyle()
          .setBorderColor(org.eclipse.che.ide.api.theme.Style.theme.tabsPanelBackground());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
  }

  interface PartButtonWidgetUiBinder extends UiBinder<Widget, PartButtonWidget> {}
}
