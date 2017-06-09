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
package org.eclipse.che.plugin.maven;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Provides MAVEN_OPTS environment variable with a value set to either maven options
 * from configuration or java options if maven options are not set.
 *
 * @author Yevhenii Voevodin
 */
public class MavenOptsEnvVariableProvider implements Provider<String> {

    @Inject
    @Named("che.workspace.java.options")
    private String javaOpts;

    @Inject
    @Named("che.workspace.maven.options")
    @Nullable
    private String mavenOpts;

    @Override
    public String get() {
        return "MAVEN_OPTS=" + (mavenOpts == null ? javaOpts : mavenOpts);
    }
}
