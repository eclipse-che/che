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
package org.eclipse.che.ide.api.editor.codeassist;

import elemental.dom.Element;

/**
 * Action triggered when completion proposal additional info must be displayed.
 */
public interface AdditionalInfoCallback {

    /**
     * Display the proposal additional info.
     * @param pixelX the x coordinate
     * @param pixelY the y coordinate
     * @param info the info message to show
     * @return the element used to display the information
     */
    Element onAdditionalInfoNeeded(float pixelX, float pixelY, Element infoWidget);
}
