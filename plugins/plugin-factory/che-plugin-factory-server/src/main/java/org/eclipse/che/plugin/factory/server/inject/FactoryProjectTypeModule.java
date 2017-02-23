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
package org.eclipse.che.plugin.factory.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.factory.server.FactoryProjectType;

/**
 * Factory project type binding
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DynaModule
public class FactoryProjectTypeModule extends AbstractModule {
    @Override
    protected void configure() {
        final Multibinder<ProjectType> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType.class);
        projectTypeMultibinder.addBinding().to(FactoryProjectType.class);
    }
}
