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
package org.eclipse.che.ide.ext.help.client;


import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.ext.help.client.about.ShowAboutAction;

import javax.inject.Inject;
import javax.inject.Singleton;

/** @author Vitalii Parfonov */
@Singleton
@Extension(title = "Help Extension", version = "3.0.0")
public class HelpAboutExtension {


    @Inject
    public HelpAboutExtension(ActionManager actionManager,
                              final ShowAboutAction showAboutAction,
                              final RedirectToSupportAction redirectToSupportAction) {

        // Compose Help menu
        DefaultActionGroup helpGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_HELP);
        actionManager.registerAction("showAbout", showAboutAction);
        actionManager.registerAction("redirectToSupport", redirectToSupportAction);

        helpGroup.addSeparator();
        helpGroup.add(redirectToSupportAction);
        helpGroup.add(showAboutAction);
    }
}
