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
 * Represents an entire binary type (single <code>.class</code> file).
 * A class file has a single child of type <code>IType</code>.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ClassFile extends TypeRoot, LabelElement {

    /**
     * Returns the type contained in this class file.
     * This is a handle-only method. The type may or may not exist.
     *
     * @return the type contained in this class file
     */
    Type getType();

    void setType(Type type);
}
