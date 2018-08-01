/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.examples;

import org.eclipse.che.examples.HelloWorld;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * TestNG test for simple App.
 */
public class AppOneTest {

    @Test
    public void shouldSuccessOfAppOne() {
        assertTrue(new HelloWorld().returnTrue());
    }

    @Test
    public void shouldFailOfAppOne() {
        assertFalse(new HelloWorld().returnTrue());
    }
}
