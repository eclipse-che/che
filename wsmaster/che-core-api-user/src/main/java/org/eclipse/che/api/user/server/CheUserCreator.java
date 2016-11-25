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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.DBInitializer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Collections.emptyList;

/**
 * Creates 'che' default user.
 *
 * @author Anton Korneta
 */
@Singleton
public class CheUserCreator {

    @Inject
    private UserManager userManager;

    @Inject
    @SuppressWarnings("unused")
    private DBInitializer initializer;

    @PostConstruct
    public void createCheUser() throws ServerException {
        try {
            userManager.getById("che");
        } catch (NotFoundException ex) {
            try {
                final UserImpl cheUser = new UserImpl("che",
                                                      "che@eclipse.org",
                                                      "che",
                                                      "secret",
                                                      emptyList());
                userManager.create(cheUser, false);
            } catch (ConflictException ignore) {
            }
        }
    }
}
