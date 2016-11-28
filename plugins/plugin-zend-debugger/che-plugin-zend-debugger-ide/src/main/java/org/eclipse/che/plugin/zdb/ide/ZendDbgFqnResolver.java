/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.ide;

import static com.google.common.base.Preconditions.checkArgument;

import javax.validation.constraints.NotNull;

import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolver;

import com.google.inject.Singleton;

/**
 * PHP debug fqn resolver.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgFqnResolver implements FqnResolver {

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String resolveFqn(@NotNull final VirtualFile file) {
        if (file instanceof Resource) {
            return resolveFQN(file);
        } else {
            return file.getName();
        }
    }

    private String resolveFQN(VirtualFile file) {
        checkArgument(file instanceof File, "Given file is not resource based");
        return ((Resource) file).getName();
    }

}
