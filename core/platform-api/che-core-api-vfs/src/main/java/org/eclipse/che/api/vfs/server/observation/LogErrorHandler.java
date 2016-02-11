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
package org.eclipse.che.api.vfs.server.observation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author andrew00x
 */
public final class LogErrorHandler implements ErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LogErrorHandler.class);

    @Override
    public void onError(VirtualFileEvent event, Throwable error) {
        LOG.error(String.format("Error processing of event: %s", event), error);
    }
}


