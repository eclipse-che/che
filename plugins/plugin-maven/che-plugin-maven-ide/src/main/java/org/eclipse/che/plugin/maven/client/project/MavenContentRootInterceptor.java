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
package org.eclipse.che.plugin.maven.client.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.java.client.project.interceptor.AbstractJavaContentRootInterceptor;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;

/**
 * @author Vlad Zhukovskiy
 */
@Singleton
public class MavenContentRootInterceptor extends AbstractJavaContentRootInterceptor {

    @Inject
    public MavenContentRootInterceptor(JavaNodeManager javaResourceNodeManager) {
        super(javaResourceNodeManager);
    }

    @Override
    public String getSrcFolderAttribute() {
        return SOURCE_FOLDER;
    }

    @Override
    public String getTestSrcFolderAttribute() {
        return TEST_SOURCE_FOLDER;
    }
}
