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
package org.eclipse.che.workspace.infrastructure.docker.config.dns;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * @author Alexander Garagatyi
 */
public class DnsResolversModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<String[]>() {}).annotatedWith(Names.named("che.docker.dns_resolvers"))
                                            .toProvider(DnsResolversProvider.class);
    }
}
