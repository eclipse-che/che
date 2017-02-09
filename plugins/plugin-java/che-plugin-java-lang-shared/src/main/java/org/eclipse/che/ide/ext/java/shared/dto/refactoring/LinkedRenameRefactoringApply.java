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

/**
 * DTO for applying linked mode rename refactoring
 * @author Evgen Vidolob
 */
@DTO
public interface LinkedRenameRefactoringApply extends RefactoringSession {

    /**
     * Get new element name.
     * @return the name
     */
    String getNewName();

    /**
     * Set new element name
     * @param newName
     */
    void setNewName(String newName);
}
