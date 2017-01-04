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
package org.eclipse.che.api.factory.server;


import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;

/**
 * Interface for validations of factory urls on accept stage.
 **/
public interface FactoryAcceptValidator {

    /**
     * Validates factory object on accept stage. Implementation should throw
     * {@link BadRequestException} if factory object is invalid.
     *
     * @param factory
     *         factory object to validate
     * @throws BadRequestException
     *         in case if factory is not valid
     */
    void validateOnAccept(FactoryDto factory) throws BadRequestException;
}
