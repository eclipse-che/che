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
package org.eclipse.che.ide.ext.java.client.project.node;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.ext.java.client.project.node.jar.ExternalLibrariesNode;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarContainerNode;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFolderNode;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.ext.java.shared.Jar;
import org.eclipse.che.ide.ext.java.shared.JarEntry;

import javax.validation.constraints.NotNull;

/**
 * @author Vlad Zhukovskiy
 */
public interface JavaNodeFactory {
    ExternalLibrariesNode newExternalLibrariesNode(@NotNull ProjectConfigDto projectConfig,
                                                   @NotNull NodeSettings nodeSettings);

    JarContainerNode newJarContainerNode(@NotNull Jar jar,
                                         @NotNull ProjectConfigDto projectConfig,
                                         @NotNull NodeSettings nodeSettings);

    /**
     * Create node which might be used for any jar content.
     *
     * @param jarEntry
     *         represent a JAR file entry
     * @param libId
     *         ID of the library which contains current JAR entry.
     *         This field may be null, then content of the node will be searched by FQN
     * @param projectConfig
     *         project descriptor for the current project
     * @param nodeSettings
     *         special settings for the tree
     * @return instance of {@link JarFileNode}
     */
    JarFileNode newJarFileNode(@NotNull JarEntry jarEntry,
                               @Nullable Integer libId,
                               @NotNull ProjectConfigDto projectConfig,
                               @NotNull NodeSettings nodeSettings);

    JarFolderNode newJarFolderNode(@NotNull JarEntry jarEntry,
                                   int libId,
                                   @NotNull ProjectConfigDto projectConfig,
                                   @NotNull NodeSettings nodeSettings);

    PackageNode newPackageNode(@NotNull ItemReference itemReference,
                               @NotNull ProjectConfigDto projectConfig,
                               @NotNull JavaNodeSettings nodeSettings);

    JavaFileNode newJavaFileNode(@NotNull ItemReference itemReference,
                                 @NotNull ProjectConfigDto projectConfig,
                                 @NotNull JavaNodeSettings nodeSettings);

    SourceFolderNode newSourceFolderNode(@NotNull ItemReference itemReference,
                                         @NotNull ProjectConfigDto projectConfig,
                                         @NotNull JavaNodeSettings nodeSettings,
                                         @NotNull ContentRoot contentRootType);
}
