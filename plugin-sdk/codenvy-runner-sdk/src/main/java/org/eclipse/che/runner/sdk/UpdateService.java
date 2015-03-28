/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.runner.sdk;

import com.google.inject.Inject;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Service to update server-side code of launched Codenvy Extension.
 *
 * @author Artem Zatsarynnyy
 */
@Path("runner-sdk")
public class UpdateService {
    @Inject
    private ApplicationUpdaterRegistry applicationUpdaterRegistry;

    @Path("update/{id}")
    @POST
    public void updateApplication(@PathParam("id") long id) throws Exception {
        ApplicationUpdater updater = applicationUpdaterRegistry.getUpdaterByApplicationProcessId(id);
        if (updater != null) {
            updater.update();
        }
    }
}
