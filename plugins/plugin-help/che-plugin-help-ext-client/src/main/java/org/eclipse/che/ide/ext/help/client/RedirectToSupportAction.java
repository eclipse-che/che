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
package org.eclipse.che.ide.ext.help.client;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.ProductInfoDataProvider;

/**
 * Redirect to support window
 *
 * @author Oleksii Orel
 * @author Alexander Andrienko
 */
@Singleton
public class RedirectToSupportAction extends Action {

    private final AnalyticsEventLogger    eventLogger;
    private final ProductInfoDataProvider productInfoDataProvider;

    @Inject
    public RedirectToSupportAction(HelpExtensionLocalizationConstant locale,
                                   ProductInfoDataProvider productInfoDataProvider,
                                   AnalyticsEventLogger eventLogger,
                                   AboutResources resources) {
        super(productInfoDataProvider.getSupportTitle(), locale.actionRedirectToSupportDescription(), null, resources.getSupport());
        this.eventLogger = eventLogger;
        this.productInfoDataProvider = productInfoDataProvider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        Window.open(productInfoDataProvider.getSupportLink(), "_blank", null);
    }
}
