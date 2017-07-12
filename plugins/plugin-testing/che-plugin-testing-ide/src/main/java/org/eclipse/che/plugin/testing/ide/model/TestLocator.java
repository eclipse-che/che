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
package org.eclipse.che.plugin.testing.ide.model;

import org.eclipse.che.ide.resource.Path;

/**
 * Locator of the test.
 */
public interface TestLocator {

    TestLocation getTestLocatio(String locationUrl);

    interface TestLocation {
        /** Returns path of the test class. */
        Path getFilePath();

        /** Returns offset of the test. */
        int getOffset();
    }
}
