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
package org.eclipse.che.ide.ext.java.shared.dto.classpath;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about classpath of the project.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ClasspathEntryDTO {
    /** Returns type of the entry. */
    int getEntryKind();

    void setEntryKind(int kind);

    ClasspathEntryDTO withEntryKind(int kind);

    /** Returns path to the entry. */
    String getPath();

    void setPath(String path);

    ClasspathEntryDTO withPath(String path);

}
