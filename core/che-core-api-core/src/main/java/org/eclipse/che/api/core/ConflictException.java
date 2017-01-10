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
package org.eclipse.che.api.core;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * A {@code ConflictException} is throws when operation could not be performed because of conflict with prior state. For example, there is
 * an existing resource prevents creation of a new one.
 * <p/>
 *  Typically in REST API such errors are converted in HTTP response with status 409.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(ServiceError serviceError) {
        super(serviceError);
    }
}
