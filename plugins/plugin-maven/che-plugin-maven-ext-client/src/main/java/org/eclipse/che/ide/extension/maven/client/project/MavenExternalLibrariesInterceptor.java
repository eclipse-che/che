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
package org.eclipse.che.ide.extension.maven.client.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.ext.java.client.project.interceptor.AbstractExternalLibrariesNodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;

import java.util.List;

/**
 * @author Vlad Zhukovskiy
 */
@Singleton
public class MavenExternalLibrariesInterceptor extends AbstractExternalLibrariesNodeInterceptor {
    @Inject
    public MavenExternalLibrariesInterceptor(JavaNodeManager javaResourceNodeManager) {
        super(javaResourceNodeManager);
    }

    @Override
    public boolean show(HasProjectConfig node) {
        List<String> packaging = node.getProjectConfig().getAttributes().get(MavenAttributes.PACKAGING);
        return packaging != null && !packaging.isEmpty() && !packaging.get(0).equals("pom");

    }
}
