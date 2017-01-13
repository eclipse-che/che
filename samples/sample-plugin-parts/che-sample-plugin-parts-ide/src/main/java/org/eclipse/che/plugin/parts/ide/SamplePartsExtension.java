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
package org.eclipse.che.plugin.parts.ide;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.parts.ide.helloworldview.HelloWorldViewAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;

/**
 * Extension that defines a simple view containing a 'Hello World' label.
 *
 * @author Edgar Mueller
 */
@Extension(title = "Sample Parts Extension")
public class SamplePartsExtension {

    /**
     * Constructor.
     *
     * @param actionManager the {@link ActionManager} that is used to register the action
     * @param action the {@link HelloWorldViewAction} that display the sample view
     */
    @Inject
    public SamplePartsExtension(ActionManager actionManager, HelloWorldViewAction action){
        actionManager.registerAction("helloWorldViewAction",action);
        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);

        DefaultActionGroup sampleActionGroup = new DefaultActionGroup("Sample Action", true, actionManager);
        sampleActionGroup.add(action);

        mainMenu.add(sampleActionGroup);
    }
}
