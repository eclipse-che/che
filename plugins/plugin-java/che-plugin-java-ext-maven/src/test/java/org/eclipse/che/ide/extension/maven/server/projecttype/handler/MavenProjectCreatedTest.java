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
package org.eclipse.che.ide.extension.maven.server.projecttype.handler;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Nikitenko
 */
public class MavenProjectCreatedTest {

    private MavenProjectCreatedHandler mavenProjectCreatedHandler;

    @Before
    public void setup() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder<ProjectHandler> projectTypeResolverMultibinder = Multibinder.newSetBinder(binder(), ProjectHandler.class);
                projectTypeResolverMultibinder.addBinding().to(ProjectBecomeMavenHandler.class);
            }
        });
        mavenProjectCreatedHandler = injector.getInstance(MavenProjectCreatedHandler.class);
    }

    @Test
    public void testGetProjectType() throws Exception {
        Assert.assertEquals(MavenAttributes.MAVEN_ID, mavenProjectCreatedHandler.getProjectType());
    }

}
