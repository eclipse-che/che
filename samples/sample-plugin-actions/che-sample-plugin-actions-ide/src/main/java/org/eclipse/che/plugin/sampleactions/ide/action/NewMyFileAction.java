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
package org.eclipse.che.plugin.sampleactions.ide.action;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.sampleactions.ide.SampleActionsResources;

/**
 * Simple action that creates an empty file with an "my" extension.
 */
public class NewMyFileAction extends AbstractNewResourceAction {

    /**
     * Creates new action.
     */
    @Inject
    public NewMyFileAction(SampleActionsResources resources,
                           DialogFactory dialogFactory,
                           CoreLocalizationConstant coreLocalizationConstant,
                           EventBus eventBus,
                           AppContext appContext,
                           NotificationManager notificationManager) {
        super("Create my file", "Create a new file",
                resources.icon(),
                dialogFactory,
                coreLocalizationConstant,
                eventBus,
                appContext,
                notificationManager);
    }

    @Override
    protected String getExtension() {
        return "my";
    }
}
