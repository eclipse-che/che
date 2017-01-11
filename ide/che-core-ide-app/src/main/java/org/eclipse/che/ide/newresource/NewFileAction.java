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
package org.eclipse.che.ide.newresource;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Action to create new file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewFileAction extends AbstractNewResourceAction {
    @Inject
    public NewFileAction(CoreLocalizationConstant localizationConstant,
                         Resources resources,
                         DialogFactory dialogFactory,
                         EventBus eventBus,
                         AppContext appContext,
                         NotificationManager notificationManager) {
        super(localizationConstant.actionNewFileTitle(),
              localizationConstant.actionNewFileDescription(),
              resources.defaultFile(), dialogFactory, localizationConstant, eventBus, appContext, notificationManager);
    }
}
