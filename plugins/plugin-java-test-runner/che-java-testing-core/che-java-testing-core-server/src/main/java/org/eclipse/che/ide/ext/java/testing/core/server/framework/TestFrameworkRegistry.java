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
package org.eclipse.che.ide.ext.java.testing.core.server.framework;

import com.google.inject.Inject;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for Test Frameworks on the server. All the Test Frameworks should be registered here
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestFrameworkRegistry {

    private final Map<String, TestRunner> frameworks = new HashMap<>();


    @Inject
    public TestFrameworkRegistry(Set<TestRunner> runners) {
        runners.forEach(this::register);
    }

    private void register(@NotNull TestRunner handler) {
        frameworks.put(handler.getName(), handler);
    }

    /**
     * Get the registered framework by name.
     *
     * @param frameworkName name of the framework.
     * @return the TestRunner implementation of the framework if available, otherwise null.
     */
    public TestRunner getTestRunner(String frameworkName) {
        return frameworks.get(frameworkName);
    }

}
