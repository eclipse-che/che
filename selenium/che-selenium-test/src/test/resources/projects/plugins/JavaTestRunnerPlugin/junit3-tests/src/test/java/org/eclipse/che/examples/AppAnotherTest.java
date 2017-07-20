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
package org.eclipse.che.examples;

import junit.framework.TestCase;

import org.eclipse.che.examples.HelloWorld;

/**
 * Unit test for simple App.
 */
public class AppAnotherTest extends TestCase {

    public void testAppAnotherShouldSuccess() {
        assertTrue(new HelloWorld().returnTrue());
    }

    public void testAppAnotherShouldFail() {
        assertFalse(new HelloWorld().returnTrue());
    }
}
