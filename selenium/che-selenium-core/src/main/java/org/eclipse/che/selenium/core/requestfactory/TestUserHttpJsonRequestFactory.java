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
package org.eclipse.che.selenium.core.requestfactory;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author Dmytro Nochevnov
 */
public class TestUserHttpJsonRequestFactory extends TestHttpJsonRequestFactory {

    private final String authToken;

    public TestUserHttpJsonRequestFactory(@NotNull String authToken) {
        Objects.requireNonNull(authToken);
        this.authToken = authToken;
    }

    protected String getAuthToken() {
        return this.authToken;
    }
}
