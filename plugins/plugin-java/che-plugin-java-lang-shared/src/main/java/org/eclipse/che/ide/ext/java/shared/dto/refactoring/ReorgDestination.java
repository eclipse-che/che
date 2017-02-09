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
 * DTO for setting destination for reorg refactorings.
 * @author Evgen Vidolob
 */
@DTO
public interface ReorgDestination extends RefactoringSession {

    /**
     * Workspace path of the java project
     * @return the path
     */
    String getProjectPath();

    /**
     * Set workspace path of the project
     * @param projectPath
     */
    void setProjectPath(String projectPath);

    /**
     * @return the destination
     */
    String getDestination();

    /**
     * Set destination path
     * @param destination
     */
    void setDestination(String destination);

    /**
     * Set destination type.
     * @param type the type of destination
     */
    void setType(DestinationType type);

    /**
     * @return the destination type
     */
    DestinationType getType();

    enum DestinationType {
        PACKAGE, SOURCE_REFERENCE, RESOURCE
    }
}
