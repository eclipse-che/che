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
package org.eclipse.che.plugin.sample.wizard.ide;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.plugin.sample.wizard.ide.action.NewXFileAction;
import org.eclipse.che.plugin.sample.wizard.ide.action.SampleAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_HELP;

/**
 *
 *
 * */
@Extension(title = "Sample Wizard")
public class SampleWizardExtension {

    public static String X_CATEGORY = "Sample Category";

    @Inject
    public SampleWizardExtension(FileTypeRegistry fileTypeRegistry,
                                 @Named("XFileType") FileType xFile) {
        fileTypeRegistry.registerFileType(xFile);
    }

    @Inject
    private void prepareActions(SampleAction sampleAction,
                                NewXFileAction newXFileAction,
                                ActionManager actionManager) {

        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_HELP);
        DefaultActionGroup newFileGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);

        actionManager.registerAction("sayHello", sampleAction);
        actionManager.registerAction("newFileActon", newXFileAction);
        newGroup.add(sampleAction, Constraints.FIRST);
        newFileGroup.add(newXFileAction, Constraints.FIRST);

    }

}
