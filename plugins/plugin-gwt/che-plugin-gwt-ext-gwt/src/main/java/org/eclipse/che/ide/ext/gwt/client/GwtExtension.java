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
package org.eclipse.che.ide.ext.gwt.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;

/**
 * Entry point for an extension that adds support to work with GWT projects.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
@Extension(title = "GWT", version = "1.0.0")
public class GwtExtension {

    @Inject
    public GwtExtension() {
    }
}
