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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * DTO for creating move refactoring session
 * @author Evgen Vidolob
 */
@DTO
public interface CreateMoveRefactoring {

    /**
     * Gets elements that will be moved.
     *
     * @return the elements
     */
    List<ElementToMove> getElements();

    void setElements(List<ElementToMove> elements);

    /**
     * Gets project path.
     *
     * @return the project path
     */
    String getProjectPath();

    void setProjectPath(String projectPath);
}
