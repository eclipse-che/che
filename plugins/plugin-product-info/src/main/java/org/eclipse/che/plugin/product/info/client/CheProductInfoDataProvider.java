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
package org.eclipse.che.plugin.product.info.client;

import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.ProductInfoDataProviderImpl;
import org.eclipse.che.ide.ui.Resources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Implementation of {@link ProductInfoDataProvider}
 *
 * @author Alexander Andrienko
 */
@Singleton
public class CheProductInfoDataProvider extends ProductInfoDataProviderImpl {

  private final LocalizationConstant locale;
  private final Resources resources;

  @Inject
  public CheProductInfoDataProvider(LocalizationConstant locale, Resources resources) {
    this.locale = locale;
    this.resources = resources;
  }

  @Override
  public String getName() {
    return locale.getProductName();
  }

  @Override
  public String getSupportLink() {
    return locale.getSupportLink();
  }

  @Override
  public String getDocumentTitle() {
    return locale.cheTabTitle();
  }

  @Override
  public String getDocumentTitle(String workspaceName) {
    return locale.cheTabTitle(workspaceName);
  }

  @Override
  public SVGResource getLogo() {
    return resources.logo();
  }

  @Override
  public SVGResource getWaterMarkLogo() {
    return resources.waterMarkLogo();
  }

  @Override
  public String getSupportTitle() {
    return locale.supportTitle();
  }
}
