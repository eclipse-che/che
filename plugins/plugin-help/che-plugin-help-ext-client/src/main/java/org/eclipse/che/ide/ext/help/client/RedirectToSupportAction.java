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
package org.eclipse.che.ide.ext.help.client;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.util.browser.BrowserUtils;

/**
 * Redirect to support window
 *
 * @author Oleksii Orel
 * @author Alexander Andrienko
 */
@Singleton
public class RedirectToSupportAction extends BaseAction {
  private final ProductInfoDataProvider productInfoDataProvider;

  @Inject
  public RedirectToSupportAction(
      HelpExtensionLocalizationConstant locale,
      ProductInfoDataProvider productInfoDataProvider,
      AboutResources resources) {
    super(
        productInfoDataProvider.getSupportTitle(),
        locale.actionRedirectToSupportDescription(),
        resources.getSupport());
    this.productInfoDataProvider = productInfoDataProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    BrowserUtils.openInNewTab(productInfoDataProvider.getSupportLink());
  }

  @Override
  public void update(ActionEvent event) {
    event.getPresentation().setVisible(!isNullOrEmpty(productInfoDataProvider.getSupportLink()));
  }
}
