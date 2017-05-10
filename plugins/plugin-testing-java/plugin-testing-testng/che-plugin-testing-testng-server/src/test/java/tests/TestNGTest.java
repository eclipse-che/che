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
package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class TestNGTest {


    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("SetUp");
    }

    @Test
    public void testName() throws Exception {
        System.out.println("Hello World!!!");
        Assert.assertTrue(false);
    }
}
