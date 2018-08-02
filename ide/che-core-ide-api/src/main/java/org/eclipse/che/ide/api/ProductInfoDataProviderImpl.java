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
package org.eclipse.che.ide.api;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitalii Parfonov */
public class ProductInfoDataProviderImpl implements ProductInfoDataProvider {
  @Override
  public String getName() {
    return "Che";
  }

  @Override
  public String getSupportLink() {
    return "";
  }

  @Override
  public String getDocumentTitle() {
    return "";
  }

  @Override
  public String getDocumentTitle(String workspaceName) {
    return workspaceName;
  }

  @Override
  public SVGResource getLogo() {
    return null;
  }

  @Override
  public SVGResource getWaterMarkLogo() {
    return null;
  }

  @Override
  public String getSupportTitle() {
    return "";
  }
}
