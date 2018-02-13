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
package org.eclipse.che.ide.ext.ssh.client.manage;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

/**
 * The class contains logic which allows us display SSH public key {@link ClipboardButtonBuilder},
 * which allows to store values from text fields to browser clip board.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public final class ShowSshKeyViewImpl extends Window implements ShowSshKeyView {
  interface ShowSshKeyViewImplUiBinder extends UiBinder<Widget, ShowSshKeyViewImpl> {}

  @UiField(provided = true)
  final CoreLocalizationConstant locale;

  @UiField(provided = true)
  final SshLocalizationConstant constant;

  @UiField FlowPanel keyPanel;
  @UiField TextBox key;

  @Inject
  public ShowSshKeyViewImpl(
      ShowSshKeyViewImplUiBinder binder,
      CoreLocalizationConstant locale,
      ClipboardButtonBuilder clipBoardBtnBuilder,
      SshLocalizationConstant constant) {
    this.locale = locale;
    this.constant = constant;

    setWidget(binder.createAndBindUi(this));

    clipBoardBtnBuilder.withResourceWidget(key).build();

    addButtons();
  }

  private void addButtons() {
    addFooterButton(locale.cancel(), "copy-machine-key-cancel-button", event -> hide());
  }

  @Override
  public void show(String name, String key) {
    setTitle(constant.publicSshKeyField() + name);
    this.key.setText(key);
    this.key.setReadOnly(true);
    super.show();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    // to do nothing
  }
}
