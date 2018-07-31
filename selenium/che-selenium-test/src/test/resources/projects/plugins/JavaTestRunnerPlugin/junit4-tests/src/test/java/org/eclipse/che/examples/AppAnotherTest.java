/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.examples;

import org.eclipse.che.examples.HelloWorld;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppAnotherTest {

    @Test
    public void shouldSuccessOfAppAnother() {
        assertTrue(new HelloWorld().returnTrue());
    }

    @Test
    public void shouldFailOfAppAnother() {
        assertFalse(new HelloWorld().returnTrue());
    }

    @Test
    @Ignore
    public void shouldBeIgnoredOfAppAnother() { Assert.fail();}
}
