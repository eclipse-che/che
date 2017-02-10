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
 * Common protocol for all elements provided by the Java model.
 * All model inspired by Eclipse JDT Java Model.
 *
 * @author Evgen Vidolob
 */
public interface JavaElement {

    /**
     * Returns the name of this element. This is a handle-only method.
     *
     * @return the element name
     */
    String getElementName();

    void setElementName(String name);


    /**
     * Returns a string representation of this element handle. The format of
     * the string is not specified; however, the identifier is stable across
     * workspace sessions, and can be used to recreate this handle via the
     * <code>JavaCore.create(String)</code> method.
     *
     * @return the string handle identifier
     */
    String getHandleIdentifier();

    void setHandleIdentifier(String identifier);
}
