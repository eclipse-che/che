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
 * Base interface for all refactoring DTO's.
 * @author Evgen Vidolob
 */
@DTO
public interface RefactoringSession {

    /**
     * Refactoring session id.
     * @return the id of this session.
     */
    String getSessionId();

    /**
     * Set id for this session.
     * @param sessionId the id of this session.
     */
    void setSessionId(String sessionId);
}
