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
package org.eclipse.che.plugin.python.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.python.ide.PythonLocalizationConstant;
import org.eclipse.che.plugin.python.ide.PythonResources;

import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_EXT;

/**
 * Action to create new Python source file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class CreatePythonFileAction extends AbstractNewResourceAction {

    @Inject
    public CreatePythonFileAction(PythonLocalizationConstant localizationConstant,
                                  PythonResources pythonResources,
                                  DialogFactory dialogFactory,
                                  CoreLocalizationConstant coreLocalizationConstant,
                                  EventBus eventBus,
                                  AppContext appContext,
                                  NotificationManager notificationManager) {
        super(localizationConstant.createPythonFileActionTitle(),
              localizationConstant.createPythonFileActionDescription(),
              pythonResources.pythonFile(), dialogFactory, coreLocalizationConstant, eventBus, appContext, notificationManager);
    }

    @Override
    protected String getExtension() {
        return PYTHON_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return "";
    }

}
