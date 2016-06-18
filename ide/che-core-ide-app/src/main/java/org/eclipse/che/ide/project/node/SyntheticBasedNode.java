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
package org.eclipse.che.ide.project.node;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;

import javax.validation.constraints.NotNull;

/**
 * Base class for the synthetic, non-resourced item
 *
 * @author Vlad Zhukovskiy
 */
public abstract class SyntheticBasedNode<DataObject> extends AbstractProjectBasedNode<DataObject> {

    public SyntheticBasedNode(@NotNull DataObject dataObject,
                              @NotNull ProjectConfigDto projectConfig,
                              @NotNull NodeSettings nodeSettings) {
        super(dataObject, projectConfig, nodeSettings);
    }

}
