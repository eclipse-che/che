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
package org.eclipse.che.ide.xml;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Action to create new XML file.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NewXmlFileAction extends AbstractNewResourceAction {
    private static final String DEFAULT_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    @Inject
    public NewXmlFileAction(CoreLocalizationConstant localizationConstant,
                            Resources resources,
                            DialogFactory dialogFactory,
                            CoreLocalizationConstant coreLocalizationConstant,
                            EventBus eventBus,
                            AppContext appContext,
                            NotificationManager notificationManager) {
        super(localizationConstant.actionNewXmlFileTitle(),
              localizationConstant.actionNewXmlFileDescription(),
              resources.defaultFile(), dialogFactory, coreLocalizationConstant, eventBus, appContext, notificationManager);
    }

    @Override
    protected String getExtension() {
        return "xml";
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
