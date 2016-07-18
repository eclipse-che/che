/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.languageserver.shared.model.impl;

import org.eclipse.che.plugin.languageserver.shared.model.ServerConfigurationUrls;

import java.util.List;
import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class ServerConfigurationUrlsImpl implements ServerConfigurationUrls {
    private final List<String> urls;

    public ServerConfigurationUrlsImpl(List<String> urls) {this.urls = urls;}

    @Override
    public List<String> getUrls() {
        return urls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerConfigurationUrlsImpl)) return false;
        ServerConfigurationUrlsImpl that = (ServerConfigurationUrlsImpl)o;
        return Objects.equals(urls, that.urls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urls);
    }
}
