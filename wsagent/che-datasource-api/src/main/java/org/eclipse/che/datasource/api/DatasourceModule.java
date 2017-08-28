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
package org.eclipse.che.datasource.api;

import com.google.inject.AbstractModule;
import org.eclipse.che.inject.DynaModule;

@DynaModule
public class DatasourceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JdbcConnectionFactory.class);
        bind(AvailableDriversService.class);
        bind(TestConnectionService.class);
    }

}
