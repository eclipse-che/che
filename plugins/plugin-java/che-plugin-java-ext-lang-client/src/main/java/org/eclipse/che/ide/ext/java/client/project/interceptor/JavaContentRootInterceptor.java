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
package org.eclipse.che.ide.ext.java.client.project.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;

import static org.eclipse.che.ide.ext.java.shared.Constants.DEFAULT_SOURCE_FOLDER;

/**
 * Interceptor for showing only java source folder.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaContentRootInterceptor extends AbstractJavaContentRootInterceptor {

    @Inject
    public JavaContentRootInterceptor(JavaNodeManager javaResourceNodeManager) {
        super(javaResourceNodeManager);
    }

    @Override
    public String getSrcFolderAttribute() {
        return DEFAULT_SOURCE_FOLDER;
    }

    @Override
    public String getTestSrcFolderAttribute() {
        return "";
    }
}
