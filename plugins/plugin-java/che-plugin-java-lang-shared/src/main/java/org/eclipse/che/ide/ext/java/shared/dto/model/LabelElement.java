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
package org.eclipse.che.ide.ext.java.shared.dto.model;

/**
 * Common protocol for all element that have label( label is text of element description that ready to display)
 *
 * @author Evgen Vidolob
 */
public interface LabelElement {

    /**
     * Returns the text for the label
     * @return the text string used to label the element, or <code>null</code>
     *   if there is no text label for the given object
     */
    String getLabel();

    void setLabel(String label);

}
