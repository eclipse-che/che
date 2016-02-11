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
package org.eclipse.che.ide.projectimport;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.ide.commons.exception.JobNotFoundException;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.dto.DtoFactory;

/**
 * The class contains business logic which allows define type of error.
 *
 * @author Dmitry Shnurenko
 */
public class ErrorMessageUtils {

    private static DtoFactory dtoFactory = new DtoFactory();

    private ErrorMessageUtils() {
        throw new UnsupportedOperationException("You can not create instance of Util class.");
    }

    /**
     * The method defines error type and returns error message from passed exception.
     *
     * @param exception
     *         passed exception
     * @return error message
     */
    public static String getErrorMessage(Throwable exception) {
        if (exception instanceof JobNotFoundException) {
            return "Project import failed";
        } else if (exception instanceof UnauthorizedException) {
            UnauthorizedException unauthorizedException = (UnauthorizedException)exception;
            ServiceError serverError = dtoFactory.createDtoFromJson(unauthorizedException.getResponse().getText(),
                                                                    ServiceError.class);
            return serverError.getMessage();
        } else {
            return dtoFactory.createDtoFromJson(exception.getMessage(), ServiceError.class).getMessage();
        }
    }
}
