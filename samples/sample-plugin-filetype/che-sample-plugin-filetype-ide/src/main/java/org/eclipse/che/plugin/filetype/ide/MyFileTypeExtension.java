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
package org.eclipse.che.plugin.filetype.ide;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.plugin.filetype.ide.action.CreateMyFileAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

/**
 * Simple extension that registers the custom "MyFileType" and
 * an action to create files of that type.
 *
 * @author Edgar Mueller
 */
@Extension(title = "My FileType Extension")
public class MyFileTypeExtension {

    @Inject
    public void MyFileTypeExtension(
            final ActionManager actionManager,
            final CreateMyFileAction createMyFileAction,
            final FileTypeRegistry fileTypeRegistry,
            final @Named("MyFileType") FileType myFileType) {

        actionManager.registerAction("createMyFileAction", createMyFileAction);
        DefaultActionGroup mainContextMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        mainContextMenu.add(createMyFileAction);

        fileTypeRegistry.registerFileType(myFileType);
    }
}
