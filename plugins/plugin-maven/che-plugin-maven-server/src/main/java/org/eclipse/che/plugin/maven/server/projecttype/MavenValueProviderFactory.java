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
package org.eclipse.che.plugin.maven.server.projecttype;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;

import javax.inject.Inject;

/**
 * @author Evgen Vidolob
 */
public class MavenValueProviderFactory implements ValueProviderFactory {

    @Inject
    MavenProjectManager mavenProjectManager;


    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new MavenValueProvider(mavenProjectManager, projectFolder);
    }

}
