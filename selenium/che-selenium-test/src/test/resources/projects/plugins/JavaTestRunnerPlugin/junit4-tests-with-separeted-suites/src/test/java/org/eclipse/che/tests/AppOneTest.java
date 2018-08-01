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
package org.eclipse.che.tests;

import org.eclipse.che.example.HelloWorld;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
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

    @Test
    @Ignore
    public void shouldBeIgnoredOfAppOne(){ Assert.fail();}
}
