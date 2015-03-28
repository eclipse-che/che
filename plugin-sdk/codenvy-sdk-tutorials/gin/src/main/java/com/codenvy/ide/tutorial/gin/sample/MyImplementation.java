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
package com.codenvy.ide.tutorial.gin.sample;

import org.eclipse.che.ide.util.loging.Log;
import com.google.inject.Inject;

/**
 * The implementation of {@link MyInterface}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class MyImplementation implements MyInterface {

    @Inject
    public MyImplementation() {
    }

    /** {@inheritDoc} */
    @Override
    public void doSomething() {
        Log.info(MyImplementation.class, "my implementation");
    }
}