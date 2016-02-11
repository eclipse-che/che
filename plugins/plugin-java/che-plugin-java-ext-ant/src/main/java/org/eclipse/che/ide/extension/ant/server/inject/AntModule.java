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
package org.eclipse.che.ide.extension.ant.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.extension.ant.server.project.type.AntProjectGenerator;
import org.eclipse.che.ide.extension.ant.server.project.type.AntProjectType;
import org.eclipse.che.ide.extension.ant.server.project.type.AntValueProviderFactory;
import org.eclipse.che.inject.DynaModule;

/**
 * @author Vladyslav Zhukovskii
 * @author Dmitry Shnurenko
 */
@DynaModule
public class AntModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        Multibinder<ValueProviderFactory> multiBinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
        multiBinder.addBinding().to(AntValueProviderFactory.class);

        Multibinder<ProjectType> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType.class);
        projectTypeMultibinder.addBinding().to(AntProjectType.class);

        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(AntProjectGenerator.class);
    }
}
