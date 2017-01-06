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
package org.eclipse.che.ide.ext.web.html;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.web.WebLocalizationConstant;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Action to create new HTML file.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NewHtmlFileAction extends AbstractNewResourceAction {
    private static final String DEFAULT_CONTENT = "<!DOCTYPE html>\n" +
                                                  "<html>\n" +
                                                  "<head>\n" +
                                                  "    <title></title>\n" +
                                                  "</head>\n" +
                                                  "<body>\n" +
                                                  "\n" +
                                                  "</body>\n" +
                                                  "</html>";

    @Inject
    public NewHtmlFileAction(WebLocalizationConstant localizationConstant,
                             DialogFactory dialogFactory,
                             CoreLocalizationConstant coreLocalizationConstant,
                             EventBus eventBus,
                             AppContext appContext,
                             NotificationManager notificationManager) {
        super(localizationConstant.newHtmlFileActionTitle(),
              localizationConstant.newHtmlFileActionDescription(),
              null, dialogFactory, coreLocalizationConstant, eventBus, appContext, notificationManager);
    }

    @Override
    protected String getExtension() {
        return "html";
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
