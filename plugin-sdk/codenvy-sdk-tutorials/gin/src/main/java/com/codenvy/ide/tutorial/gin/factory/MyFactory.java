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
package com.codenvy.ide.tutorial.gin.factory;

import com.codenvy.ide.tutorial.gin.factory.assited.SomeInterface;

/**
 * The factory for creating instances of {@link MyFactoryClass} and {@link SomeInterface}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface MyFactory {
    /**
     * Create an instance of {@link MyFactoryClass} with a given string value.
     *
     * @param someText
     *         string value that needs to be injected in the creating instance
     * @return an instance of {@link MyFactoryClass}
     */
    MyFactoryClass createMyFactoryClass(String someText);

    /**
     * Create an instance of {@link SomeInterface} with a given string value.
     *
     * @param text
     *         string value that needs to be injected in the creating instance
     * @return an instance of {@link SomeInterface}
     */
    SomeInterface createSomeInterface(String text);
}