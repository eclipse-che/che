/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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
     * Get product name
     * @return product name
     */
    String getName();

    /**
     * Get support link
     * @return url to support resource
     */
    String getSupportLink();

    /**
     * Get document title for browser tab
     * @return document title
     */
    String getDocumentTitle();

    /**
     * Get document title with project name for browser tab
     * @param project name of project which was selected in the Project Explorer
     * @return document title
     */
    String getDocumentTitle(String project);

    /**
     * Get logo SVG resources
     * @return SVG resources
     */
    SVGResource getLogo();
}
