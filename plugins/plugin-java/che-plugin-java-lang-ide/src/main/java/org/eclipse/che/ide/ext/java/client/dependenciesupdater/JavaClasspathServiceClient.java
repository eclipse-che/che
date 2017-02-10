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
package org.eclipse.che.ide.ext.java.client.dependenciesupdater;

import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

/**
 * Client for Java classpath service.
 *
 * @author Artem Zatsarynnyi
 */
public interface JavaClasspathServiceClient {

    /**
     * Update project dependencies.
     *
     * @param projectPath
     *         path to the project to update its dependencies
     * @param callback
     *         the callback to use for the response
     */
    void updateDependencies(String projectPath, RequestCallback<ClassPathBuilderResult> callback);
}
