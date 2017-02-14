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
package org.eclipse.che.api.machine.server.jpa;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;

/**
 * @author Yevhenii Voevodin
 */
public class MachineJpaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RecipeDao.class).to(JpaRecipeDao.class);
        bind(SnapshotDao.class).to(JpaSnapshotDao.class);
    }
}
