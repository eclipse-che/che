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
package org.eclipse.che.ide.websocket.rest.exceptions;

import org.eclipse.che.ide.websocket.Message;

/**
 * Thrown when there was a HTTP Status-Code 401 (Unauthorized) was received.
 *
 * @author Artem Zatsarynnyi
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends ServerException {

    public UnauthorizedException(Message message) {
        super(message);
    }


}