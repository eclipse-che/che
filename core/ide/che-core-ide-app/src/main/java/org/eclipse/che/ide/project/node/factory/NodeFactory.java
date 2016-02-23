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
package org.eclipse.che.ide.project.node.factory;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;

/**
 * Factory that helps to create nodes.
 *
 * @author Vlad Zhukovskiy
 */
public interface NodeFactory {
    /**
     * Creates project node that represent opened project.
     *
     * @param projectConfig
     *         instance of {@link ProjectConfigDto} related to this node
     * @param nodeSettings
     *         node view settings
     * @return instance of {@link org.eclipse.che.ide.project.node.ProjectNode}
     */
    ProjectNode newProjectNode(ProjectConfigDto projectConfig, NodeSettings nodeSettings);

    /**
     * Creates folder referenced node.
     *
     * @param itemReference
     *         instance of {@link org.eclipse.che.api.project.shared.dto.ItemReference} related to this node
     * @param projectConfig
     *         instance of {@link ProjectConfigDto} related to this node
     * @param nodeSettings
     *         node view settings
     * @return instance of {@link FolderReferenceNode}
     */
    FolderReferenceNode newFolderReferenceNode(ItemReference itemReference,
                                               ProjectConfigDto projectConfig,
                                               NodeSettings nodeSettings);

    /**
     * Creates file referenced node.
     *
     * @param itemReference
     *         instance of {@link org.eclipse.che.api.project.shared.dto.ItemReference} related to this node
     * @param projectConfig
     *         instance of {@link ProjectConfigDto} related to this node
     * @param nodeSettings
     *         node view settings
     * @return instance of {@link FileReferenceNode}
     */
    FileReferenceNode newFileReferenceNode(ItemReference itemReference,
                                           ProjectConfigDto projectConfig,
                                           NodeSettings nodeSettings);
}
