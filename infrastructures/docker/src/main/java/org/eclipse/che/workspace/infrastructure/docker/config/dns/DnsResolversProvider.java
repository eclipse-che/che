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

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Injects DNS resolvers and ensures that it is neither empty array nor single value array with null or empty string.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DnsResolversProvider implements Provider<String[]> {
    private final String[] dnsResolvers;

    @Inject
    public DnsResolversProvider(@Nullable @Named("che.dns.resolvers") String[] dnsResolvers) {
        if (dnsResolvers == null ||
            dnsResolvers.length == 0 ||
            (dnsResolvers.length == 1 && isNullOrEmpty(dnsResolvers[0]))) {
            this.dnsResolvers = null;
        } else {
            this.dnsResolvers = dnsResolvers;
        }
    }

    @Nullable
    @Override
    public String[] get() {
        return dnsResolvers;
    }
}
