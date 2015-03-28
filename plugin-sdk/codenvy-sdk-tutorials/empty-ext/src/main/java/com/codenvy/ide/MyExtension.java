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
package com.codenvy.ide;

import org.eclipse.che.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** The skeleton of empty Codenvy extension. */
@Singleton
@Extension(title = "Empty extension", version = "1.0.0")
public class MyExtension {

    @Inject
    public MyExtension() {
    }
}