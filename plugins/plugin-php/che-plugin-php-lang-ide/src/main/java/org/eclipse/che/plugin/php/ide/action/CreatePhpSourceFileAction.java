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
package org.eclipse.che.plugin.php.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.php.ide.PhpLocalizationConstant;
import org.eclipse.che.plugin.php.ide.PhpResources;
import org.eclipse.che.plugin.php.shared.Constants;

/**
 * Action to create new PHP source file.
 *
 * @author Kaloyan Raev
 */
@Singleton
public class CreatePhpSourceFileAction extends NewPhplikeResourceAction {


    private static final String DEFAULT_CONTENT = "<?php\n" +
                                                  "\n";

    @Inject
    public CreatePhpSourceFileAction(PhpLocalizationConstant localizationConstant,
                                     PhpResources resources,
                                     DialogFactory dialogFactory,
                                     CoreLocalizationConstant coreLocalizationConstant,
                                     EventBus eventBus,
                                     AppContext appContext,
                                     NotificationManager notificationManager) {
        super(localizationConstant.createPhpFileActionTitle(),
              localizationConstant.createPhpFileActionDescription(),
              resources.phpFile(),
              dialogFactory,
              coreLocalizationConstant,
              eventBus,
              appContext,
              notificationManager);
    }

    @Override
    protected String getExtension() {
        return Constants.PHP_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
