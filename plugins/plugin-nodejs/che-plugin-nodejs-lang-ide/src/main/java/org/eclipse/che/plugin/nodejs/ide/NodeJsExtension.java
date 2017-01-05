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
package org.eclipse.che.plugin.nodejs.ide;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.nodejs.ide.action.NewNodeJsFileAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;

/**
 * @author Dmitry Shnurenko
 */
@Extension(title = "NodeJs")
public class NodeJsExtension {

    public static final String NODE_JS_CATEGORY = "Node.js";

    @Inject
    private void prepareActions(ActionManager actionManager,
                                NodeJsResources resources,
                                IconRegistry iconRegistry,
                                NewNodeJsFileAction newFileAction) {
        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        actionManager.registerAction("newNodeJsFile", newFileAction);
        newGroup.add(newFileAction, Constraints.FIRST);
        iconRegistry.registerIcon(new Icon(NODE_JS_CATEGORY + ".samples.category.icon", resources.jsIcon()));
    }
}
