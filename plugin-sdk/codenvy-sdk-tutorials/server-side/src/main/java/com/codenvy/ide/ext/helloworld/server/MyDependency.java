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
package com.codenvy.ide.ext.helloworld.server;

import javax.inject.Singleton;

/**
 * Add some logic to a server side component, making it return some text
 */


@Singleton
public class MyDependency {
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
