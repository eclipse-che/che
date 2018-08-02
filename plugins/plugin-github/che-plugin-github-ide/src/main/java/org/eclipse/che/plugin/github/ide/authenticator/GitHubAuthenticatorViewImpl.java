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
package org.eclipse.che.plugin.github.ide.authenticator;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;

/** @author Roman Nikitenko */
public class GitHubAuthenticatorViewImpl implements GitHubAuthenticatorView {

  private DialogFactory dialogFactory;
  private GitHubLocalizationConstant locale;
  private ActionDelegate delegate;

  private CheckBox isGenerateKeys;
  private DockLayoutPanel contentPanel;

  @Inject
  public GitHubAuthenticatorViewImpl(
      DialogFactory dialogFactory,
      GitHubLocalizationConstant locale,
      ProductInfoDataProvider productInfoDataProvider) {
    this.dialogFactory = dialogFactory;
    this.locale = locale;

    isGenerateKeys = new CheckBox(locale.authGenerateKeyLabel());
    isGenerateKeys.setValue(true);

    contentPanel = new DockLayoutPanel(Style.Unit.PX);
    contentPanel.addNorth(
        new InlineHTML(locale.authorizationDialogText(productInfoDataProvider.getName())), 20);
    contentPanel.addNorth(isGenerateKeys, 20);
  }

  @Override
  public void showDialog() {
    isGenerateKeys.setValue(true);
    dialogFactory
        .createConfirmDialog(
            locale.authorizationDialogTitle(),
            contentPanel,
            getConfirmCallback(),
            getCancelCallback())
        .show();
  }

  @Override
  public boolean isGenerateKeysSelected() {
    return isGenerateKeys.getValue();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return contentPanel;
  }

  private ConfirmCallback getConfirmCallback() {
    return new ConfirmCallback() {
      @Override
      public void accepted() {
        delegate.onAccepted();
      }
    };
  }

  private CancelCallback getCancelCallback() {
    return new CancelCallback() {
      @Override
      public void cancelled() {
        delegate.onCancelled();
      }
    };
  }
}
