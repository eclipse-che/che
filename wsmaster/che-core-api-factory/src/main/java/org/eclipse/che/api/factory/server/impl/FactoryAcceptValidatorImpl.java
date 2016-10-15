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
package org.eclipse.che.api.factory.server.impl;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.user.server.spi.PreferenceDao;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory accept stage validator.
 */
@Singleton
public class FactoryAcceptValidatorImpl extends FactoryBaseValidator implements FactoryAcceptValidator {

    @Override
    public void validateOnAccept(FactoryDto factory) throws BadRequestException {
        validateCurrentTimeBetweenSinceUntil(factory);
        validateProjectActions(factory);
    }
}
