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
package org.eclipse.che.ide.ext.java.jdi.client.fqn;

import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.tree.VirtualFile;

import javax.validation.constraints.NotNull;

/**
 * FQN resolver for {@link org.eclipse.che.ide.MimeType#APPLICATION_JAVA_CLASS} nodes.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class JavaClassFqnResolver implements FqnResolver {

    @NotNull
    @Override
    public String resolveFqn(@NotNull final VirtualFile file) {
        return file.getPath();
    }
}