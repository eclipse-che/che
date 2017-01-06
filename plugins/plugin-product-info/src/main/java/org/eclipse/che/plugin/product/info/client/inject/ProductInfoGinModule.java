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
package org.eclipse.che.plugin.product.info.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.machine.CheWsAgentLinksModifier;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.plugin.product.info.client.CheProductInfoDataProvider;

/**
 * @author Oleksii Orel
 */
@ExtensionGinModule
public class ProductInfoGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(ProductInfoDataProvider.class).to(CheProductInfoDataProvider.class);
        bind(WsAgentURLModifier.class).to(CheWsAgentLinksModifier.class);
    }
}
