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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * @author gazarenkov
 */
public class ProjectTypeConstraintException extends ConflictException {

    public ProjectTypeConstraintException(String message) {
        super(message);
    }

    public ProjectTypeConstraintException(ServiceError serviceError) {
        super(serviceError);
    }
}
