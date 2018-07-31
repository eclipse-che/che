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
package hello;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class SayHelloTest  extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SayHelloTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(SayHelloTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testSayHello()
    {
        SayHello sayHello = new SayHello();
        assertTrue("Hello, codenvy".equals(sayHello.sayHello("codenvy")));
    }
}
