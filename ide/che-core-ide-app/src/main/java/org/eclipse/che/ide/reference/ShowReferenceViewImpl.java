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
package org.eclipse.che.ide.reference;

import com.google.common.base.Strings;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

/**
 * The class contains business logic which allows us display fqn and path for files, folders,
 * packages nodes etc., and construct the the buttons via {@link ClipboardButtonBuilder}, which
 * allows to store values from text fields to browser clip board.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
final class ShowReferenceViewImpl extends Window implements ShowReferenceView {
  interface ShowReferenceViewImplUiBinder extends UiBinder<Widget, ShowReferenceViewImpl> {}

  @UiField(provided = true)
  final CoreLocalizationConstant locale;

  @UiField FlowPanel referencePanel;
  @UiField TextBox reference;
  @UiField FlowPanel pathPanel;
  @UiField TextBox path;

  @Inject
  public ShowReferenceViewImpl(
      ShowReferenceViewImplUiBinder binder,
      CoreLocalizationConstant locale,
      ClipboardButtonBuilder clipBoardBtnBuilder) {
    this.locale = locale;

    setTitle(locale.showReference());

    setWidget(binder.createAndBindUi(this));

    reference.setReadOnly(true);

    clipBoardBtnBuilder.withResourceWidget(reference).build();
    clipBoardBtnBuilder.withResourceWidget(path).build();

    addButtons();
  }

  private void addButtons() {
    addFooterButton(locale.cancel(), "copy-reference-cancel-button", event -> hide());
  }

  @Override
  public void show(String reference, Path path) {
    boolean hasReference = !Strings.isNullOrEmpty(reference);

    this.reference.setText(hasReference ? reference : "");
    this.referencePanel.setVisible(hasReference);
    this.path.setText(path.toString());

    show(this.reference);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    // to do nothing
  }
}
