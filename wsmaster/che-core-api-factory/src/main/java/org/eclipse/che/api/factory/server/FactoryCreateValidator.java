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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;

/**
 * Interface for validations of factory creation stage.
 *
 * @author Alexander Garagatyi
 */
public interface FactoryCreateValidator {

    /**
     * Validates factory object on creation stage. Implementation should throw
     * exception if factory object is invalid.
     *
     * @param factory
     *         factory object to validate
     * @throws BadRequestException
     *         in case if factory is not valid
     * @throws ServerException
     *         when any server error occurs
     * @throws ForbiddenException
     *         when user have no access rights for factory creation
     */
    void validateOnCreate(FactoryDto factory) throws BadRequestException, ServerException, ForbiddenException;
}
