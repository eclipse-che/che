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
package org.eclipse.che.ide.ext.java.testing.classpath.maven.server.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.java.testing.classpath.maven.server.MavenTestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.inject.DynaModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * @author Mirage Abeysekara
 */
@DynaModule
public class TestMavenClasspathGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        newSetBinder(binder(), TestClasspathProvider.class).addBinding().to(MavenTestClasspathProvider.class);
    }
}
