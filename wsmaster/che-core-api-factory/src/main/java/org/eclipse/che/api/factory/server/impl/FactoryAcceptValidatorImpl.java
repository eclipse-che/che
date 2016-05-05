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
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.user.server.dao.PreferenceDao;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory accept stage validator.
 */
@Singleton
public class FactoryAcceptValidatorImpl extends FactoryBaseValidator implements FactoryAcceptValidator {
    @Inject
    public FactoryAcceptValidatorImpl(PreferenceDao preferenceDao) {
        super(preferenceDao);
    }

    @Override
    public void validateOnAccept(Factory factory) throws BadRequestException {
        validateCurrentTimeBetweenSinceUntil(factory);
        validateProjectActions(factory);
    }
}
