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
package org.eclipse.che.plugin.nodejs.ide.action;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.nodejs.ide.NodeJsLocalizationConstant;
import org.eclipse.che.plugin.nodejs.ide.NodeJsResources;
import org.eclipse.che.plugin.nodejs.shared.Constants;

/**
 * @author Dmitry Shnurenko
 */
public class NewNodeJsFileAction extends AbstractNewResourceAction {

    private static final String DEFAULT_CONTENT = "/* eslint-env node */";

    @Inject
    public NewNodeJsFileAction(NodeJsLocalizationConstant locale,
                               NodeJsResources resources,
                               DialogFactory dialogFactory,
                               CoreLocalizationConstant coreLocalizationConstant,
                               EventBus eventBus,
                               AppContext appContext,
                               NotificationManager notificationManager) {
        super(locale.newNodeJsFileTitle(),
              locale.newNodeJsFileDescription(),
              resources.jsIcon(),
              dialogFactory,
              coreLocalizationConstant,
              eventBus,
              appContext,
              notificationManager);
    }

    @Override
    protected String getExtension() {
        return Constants.NODE_JS_FILE_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
