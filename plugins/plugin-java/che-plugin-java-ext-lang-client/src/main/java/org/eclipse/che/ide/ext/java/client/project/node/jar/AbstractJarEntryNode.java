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
package org.eclipse.che.ide.ext.java.client.project.node.jar;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.shared.JarEntry;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import static java.util.Collections.singletonList;

/**
 * @author Vlad Zhukovskiy
 */
public abstract class AbstractJarEntryNode extends AbstractJavaSyntheticNode<JarEntry> {

    protected final Integer libId;

    public AbstractJarEntryNode(@NotNull JarEntry jarEntry,
                                @Nullable Integer libId,
                                @NotNull ProjectConfigDto projectConfig,
                                @NotNull NodeSettings nodeSettings,
                                @NotNull JavaNodeManager nodeManager) {
        super(jarEntry, projectConfig, nodeSettings, nodeManager);
        this.libId = libId;

        getAttributes().put(CUSTOM_BACKGROUND_FILL, singletonList(Style.theme.projectExplorerReadonlyItemBackground()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractJarEntryNode)) return false;
        if (!super.equals(o)) return false;

        AbstractJarEntryNode that = (AbstractJarEntryNode)o;

        return Objects.equals(libId, that.libId);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + libId;
        return result;
    }
}
