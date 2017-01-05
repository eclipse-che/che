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
package org.eclipse.che.plugin.jdb.ide.fqn;

import com.google.inject.Singleton;

import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolver;

import javax.validation.constraints.NotNull;

/**
 * @author Evgen Vidolob
 * @author Anatoliy Bazko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class JavaFqnResolver implements FqnResolver {

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String resolveFqn(@NotNull final VirtualFile file) {
        if (file instanceof Resource) {
            return JavaUtil.resolveFQN(file);
        } else {
            return file.getName();
        }
    }
}
