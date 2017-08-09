/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.client.store;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

@ExtensionGinModule
public class DatasourceStoreGinModule extends AbstractGinModule {


    @Override
    protected void configure() {
        // bind the datasource manager and the datasource metadat store
        bind(DatasourceManager.class).to(DatasourceManagerPrefImpl.class).in(Singleton.class);
//        bind(DatabaseInfoStore.class).to(DatabaseInfoStoreImpl.class);
//        bind(DatabaseInfoOracle.class).to(DatabaseInfoOracleImpl.class);
//
//        GinMultibinder<PreStoreProcessor> preStoreProcessorBinder = GinMultibinder.newSetBinder(binder(), PreStoreProcessor.class);
//        preStoreProcessorBinder.addBinding().to(SortMetadataProcessor.class);
    }
}
