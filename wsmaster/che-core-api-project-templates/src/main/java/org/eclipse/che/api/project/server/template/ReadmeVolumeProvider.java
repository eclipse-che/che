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
package org.eclipse.che.api.project.server.template;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides volumes configuration of machine for default README.md file injection. Properties
 * define a folder that contains a template and a location where to mount this folder.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ReadmeVolumeProvider implements Provider<String> {
    @Inject
    @Named("project.default.readme.folder.from.path")
    private String from;

    @Inject
    @Named("project.default.readme.folder.to.path")
    private String to;

    @Override
    public String get() {
        return from + ":" + to + ":ro,Z";
    }
}
