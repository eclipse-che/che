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
package org.eclipse.che.api.core.model.workspace.compose;

/**
 * Describes how to build image for container creation.
 *
 * @author Alexander Garagatyi
 */
public interface BuildContext {
    /**
     * Build context.
     *
     * <p/> Can be git repository, url to Dockerfile.
     */
    String getContext();

    /**
     * Alternate Dockerfile.
     *
     * <p/> Needed if dockerfile has non-default name or is not placed in the root of build context.
     */
    String getDockerfile();
}
