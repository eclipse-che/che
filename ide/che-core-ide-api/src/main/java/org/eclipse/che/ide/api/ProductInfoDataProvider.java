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

/**
 * This class contains important product information (product name, logo, browser tab title, support
 * url etc.) which should be displayed in the user interface. This is information can be different
 * for every product implementation.
 *
 * @author Alexander Andrienko
 */
public interface ProductInfoDataProvider {
  /** @return product name */
  String getName();

  /** @return url to support resource */
  String getSupportLink();

  /** @return document title for browser tab */
  String getDocumentTitle();

  /**
   * Get document title with current {@code workspaceName}.
   *
   * @param workspaceName name of the current running workspace
   * @return document title
   */
  String getDocumentTitle(String workspaceName);

  /** @return logo SVG resource */
  SVGResource getLogo();

  /** @return waterMark logo */
  SVGResource getWaterMarkLogo();

  /** @return title for support action which displayed in Help menu. */
  String getSupportTitle();
}
