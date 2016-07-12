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
package org.eclipse.che.api.machine.server.jpa;

import com.google.common.reflect.Reflection;
import com.google.inject.TypeLiteral;

import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

import javax.persistence.EntityManagerFactory;

import static org.eclipse.che.api.machine.server.jpa.H2DBServerListener.ENTITY_MANAGER_FACTORY_ATTR_NAME;

/**
 * @author Anton Korneta
 */
public class JpaTckModule extends TckModule {

    @Override
    protected void configure() {
        final EntityManagerFactory factoryProxy = Reflection.newProxy(EntityManagerFactory.class, (proxy, method, args) -> {
            if (method.getName().startsWith("createEntityManager")) {
                final EntityManagerFactory factory = (EntityManagerFactory)getTestContext().getAttribute(ENTITY_MANAGER_FACTORY_ATTR_NAME);
                return factory.createEntityManager();
            }
            return null;
        });
        bind(EntityManagerFactory.class).toInstance(factoryProxy);

        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).to(RecipeJpaTckRepository.class);

        bind(RecipeDao.class).to(JpaRecipeDao.class);
    }
}
