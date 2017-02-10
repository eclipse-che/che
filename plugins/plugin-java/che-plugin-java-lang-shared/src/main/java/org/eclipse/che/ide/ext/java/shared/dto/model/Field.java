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

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a field declared in a type.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Field extends Member {
    /**
     * Returns the simple name of this field.
     * @return the simple name of this field.
     */
    String getElementName();

    void setElementName(String elementName);

}
