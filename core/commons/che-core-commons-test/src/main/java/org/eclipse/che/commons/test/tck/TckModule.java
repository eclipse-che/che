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

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.testng.ITestContext;

import java.util.ServiceLoader;

/**
 * Abstract class for those Guice {@link Module modules} which provide
 * TCK tests components, which will be injected directly into the test class.
 *
 * <p>The {@link ServiceLoader} mechanism is used for loading such modules
 * and for injecting them later. So each module which is TCK module must
 * provide the implementations list(as described by {@code ServiceLoader} mechanism)
 * in the file named <i>org.eclipse.che.commons.test.tck.TckModule</i> usually under
 * <i>test/resources/META-INF/services</i> directory, then the {@link TckListener}
 * will recognise and load it.
 *
 * @author Yevhenii Voevodin
 * @see TckListener
 */
public abstract class TckModule extends AbstractModule {

    /**
     * It is guaranteed that this field is always present and
     * can be reused by implementation, it will be set by {@link TckListener} immediately
     * after module implementation is loaded by {@link ServiceLoader}.
     */
    private ITestContext testContext;

    /** Returns the {@link ITestContext context} of currently executing test suite. */
    protected ITestContext getTestContext() {
        return testContext;
    }

    /**
     * Sets the context of currently executing test suite.
     * This method designed to be used by {@link TckListener} for setting
     * the context before installing modules.
     */
    void setTestContext(ITestContext testContext) {
        this.testContext = testContext;
    }
}
