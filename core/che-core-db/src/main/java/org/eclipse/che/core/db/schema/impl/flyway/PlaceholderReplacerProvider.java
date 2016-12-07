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
package org.eclipse.che.core.db.schema.impl.flyway;

import org.eclipse.che.inject.ConfigurationProperties;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Placeholder replacer that uses configuration properties.
 *
 * @author Yevhenii Voevodin
 */
public class PlaceholderReplacerProvider implements Provider<PlaceholderReplacer> {

    private final PlaceholderReplacer replacer;

    @Inject
    public PlaceholderReplacerProvider(ConfigurationProperties properties) {
        replacer = new PlaceholderReplacer(properties.getProperties(".*"), "${", "}");
    }

    @Override
    public PlaceholderReplacer get() {
        return replacer;
    }
}
