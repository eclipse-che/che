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
package org.eclipse.che.commons.test.tck;

import org.testng.ITestContext;

/**
 * Listener representing fake db server url injection for testing "attributes sharing"
 * using {@link ITestContext} test suite instance.
 *
 * @author Yevhenii Voevodin
 */
public class DBServerListener extends TestListenerAdapter {

    public static final String DB_SERVER_URL_ATTRIBUTE_NAME = "db_server_url";
    public static final String DB_SERVER_URL                = "localhost:12345";

    @Override
    public void onStart(ITestContext context) {
        context.setAttribute(DB_SERVER_URL_ATTRIBUTE_NAME, DB_SERVER_URL);
    }
}
