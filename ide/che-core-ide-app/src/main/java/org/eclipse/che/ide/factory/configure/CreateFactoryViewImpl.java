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
package org.eclipse.che.ide.factory.configure;

import static com.google.gwt.dom.client.Style.Unit;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.factory.FactoryResources;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

/** @author Anton Korneta */
@Singleton
public class CreateFactoryViewImpl extends Window implements CreateFactoryView {
  private static final RegExp FACTORY_NAME_PATTERN = RegExp.compile("[^A-Za-z0-9_-]");

  interface FactoryViewImplUiBinder extends UiBinder<Widget, CreateFactoryViewImpl> {}

  private final FactoryResources factoryResources;

  private ActionDelegate delegate;

  @UiField FactoryResources.Style style;
  @UiField TextBox factoryName;
  @UiField TextBox factoryLink;
  @UiField Label factoryNameLabel;
  @UiField Label factoryLinkLabel;
  @UiField Label factoryNameErrorLabel;
  @UiField Button createFactoryButton;
  @UiField FlowPanel upperPanel;
  @UiField FlowPanel lowerPanel;
  @UiField FlowPanel createFactoryPanel;
  @UiField Anchor launch;
  @UiField Anchor configure;

  private Tooltip labelsErrorTooltip;

  @Inject
  protected CreateFactoryViewImpl(
      FactoryViewImplUiBinder uiBinder,
      CoreLocalizationConstant locale,
      FactoryResources factoryResources,
      ClipboardButtonBuilder buttonBuilder) {
    this.factoryResources = factoryResources;
    setTitle(locale.createFactoryTitle());
    Widget widget = uiBinder.createAndBindUi(this);
    widget.getElement().getStyle().setPadding(0, Unit.PX);
    setWidget(widget);
    factoryNameLabel.setText(locale.createFactoryName());
    factoryLinkLabel.setText(locale.createFactoryLink());
    configure.getElement().insertFirst(factoryResources.configure().getSvg().getElement());
    launch.getElement().insertFirst(factoryResources.execute().getSvg().getElement());
    launch.addStyleName(style.launchIcon());
    configure.addStyleName(style.configureIcon());
    createFactoryButton.setEnabled(false);
    addFooterButton(
        locale.createFactoryButtonClose(),
        "projectReadOnlyGitUrl-btnClose",
        event -> delegate.onCancelClicked());
    createFactoryButton.addClickHandler(clickEvent -> delegate.onCreateClicked());
    buttonBuilder.withResourceWidget(factoryLink).build();
    factoryLink.setReadOnly(true);
    final Tooltip launchFactoryTooltip =
        Tooltip.create(
            (elemental.dom.Element) launch.getElement(),
            PositionController.VerticalAlign.TOP,
            PositionController.HorizontalAlign.MIDDLE,
            locale.createFactoryLaunchTooltip());
    launchFactoryTooltip.setShowDelayDisabled(false);

    final Tooltip configureFactoryTooltip =
        Tooltip.create(
            (elemental.dom.Element) configure.getElement(),
            PositionController.VerticalAlign.TOP,
            PositionController.HorizontalAlign.MIDDLE,
            locale.createFactoryConfigureTooltip());
    configureFactoryTooltip.setShowDelayDisabled(false);
    factoryName.getElement().setAttribute("placeholder", "new-factory-name");
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    clear();
    this.show();
  }

  @Override
  public String getFactoryName() {
    return factoryName.getText();
  }

  @Override
  public void setAcceptFactoryLink(String acceptLink) {
    factoryLink.setText(acceptLink);
    launch.getElement().setAttribute("target", "_blank");
    launch.setHref(acceptLink);
  }

  @Override
  public void setConfigureFactoryLink(String configureLink) {
    configure.getElement().setAttribute("target", "_blank");
    configure.setHref(configureLink);
  }

  @Override
  public void enableCreateFactoryButton(boolean enabled) {
    createFactoryButton.setEnabled(enabled);
  }

  @Override
  public void showFactoryNameError(@NotNull String labelMessage, @Nullable String tooltipMessage) {
    factoryName.addStyleName(factoryResources.factoryCSS().inputError());
    factoryNameErrorLabel.setText(labelMessage);
    if (labelsErrorTooltip != null) {
      labelsErrorTooltip.destroy();
    }

    if (!Strings.isNullOrEmpty(tooltipMessage)) {
      labelsErrorTooltip =
          Tooltip.create(
              (elemental.dom.Element) factoryNameErrorLabel.getElement(),
              PositionController.VerticalAlign.TOP,
              PositionController.HorizontalAlign.MIDDLE,
              tooltipMessage);
      labelsErrorTooltip.setShowDelayDisabled(false);
    }
  }

  @Override
  public void hideFactoryNameError() {
    factoryName.removeStyleName(factoryResources.factoryCSS().inputError());
    factoryNameErrorLabel.setText("");
  }

  @Override
  public void close() {
    this.hide();
  }

  @UiHandler({"factoryName"})
  public void onProjectNameChanged(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && createFactoryButton.isEnabled()) {
      delegate.onCreateClicked();
    } else {
      String name = factoryName.getValue();
      if (!Strings.isNullOrEmpty(name) && FACTORY_NAME_PATTERN.test(name)) {
        name = name.replaceAll("[^A-Za-z0-9_]", "-");
        factoryName.setValue(name);
      }
      delegate.onFactoryNameChanged(name);
    }
  }

  private void clear() {
    launch.getElement().removeAttribute("href");
    configure.getElement().removeAttribute("href");
    createFactoryButton.setEnabled(false);
    factoryName.removeStyleName(factoryResources.factoryCSS().inputError());
    factoryNameErrorLabel.setText("");
    factoryName.setText("");
    factoryLink.setText("");
  }
}
