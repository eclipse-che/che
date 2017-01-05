/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * This class contains important product information (product name, logo, browser tab title, support url etc.) which should be displayed in
 * the user interface. This is information can be different for every product implementation.
 *
 * @author Alexander Andrienko
 */
public interface ProductInfoDataProvider {
    /**
     * @return product name
     */
    String getName();

    /**
     * @return url to support resource
     */
    String getSupportLink();

    /**
     * @return document title for browser tab
     */
    String getDocumentTitle();

    /**
     * Get document title with current {@code workspaceName}.
     *
     * @param workspaceName
     *         name of the current running workspace
     * @return document title
     */
    String getDocumentTitle(String workspaceName);

    /**
     * @return logo SVG resource
     */
    SVGResource getLogo();

    /**
     * @return title for support action which displayed in Help menu.
     */
    String getSupportTitle();
}
