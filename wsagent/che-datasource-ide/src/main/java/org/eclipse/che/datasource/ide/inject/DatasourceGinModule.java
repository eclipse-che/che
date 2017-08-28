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
package org.eclipse.che.datasource.ide.inject;


import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.eclipse.che.datasource.ide.AvailableJdbcDriversService;
import org.eclipse.che.datasource.ide.AvailableJdbcDriversServiceRestImpl;
import org.eclipse.che.datasource.ide.DatasourceClientService;
import org.eclipse.che.datasource.ide.DatasourceClientServiceImpl;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardFactory;
import org.eclipse.che.datasource.ide.newDatasource.connector.DefaultNewDatasourceConnectorView;
import org.eclipse.che.datasource.ide.newDatasource.connector.DefaultNewDatasourceConnectorViewImpl;
import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnectorAgent;
import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnectorAgentImpl;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

@ExtensionGinModule
public class DatasourceGinModule extends AbstractGinModule {

    /** The name bound to the datasource rest context. */
    public static final String DATASOURCE_CONTEXT_NAME = "datasourceRestContext";

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named(DATASOURCE_CONTEXT_NAME)).to("/datasource");
        bind(DatasourceClientService.class).to(DatasourceClientServiceImpl.class)
                                           .in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(NewDatasourceWizardFactory.class));
        bind(NewDatasourceConnectorAgent.class).to(NewDatasourceConnectorAgentImpl.class).in(Singleton.class);
        bind(DefaultNewDatasourceConnectorView.class).to(DefaultNewDatasourceConnectorViewImpl.class);

        bind(AvailableJdbcDriversService.class).to(AvailableJdbcDriversServiceRestImpl.class).in(Singleton.class);
    }


}
