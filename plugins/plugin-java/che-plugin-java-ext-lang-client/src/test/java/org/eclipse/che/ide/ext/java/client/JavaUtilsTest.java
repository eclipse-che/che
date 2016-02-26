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
package org.eclipse.che.ide.ext.java.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.ext.java.client.JavaUtils.checkCompilationUnitName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.checkPackageName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.isValidCompilationUnitName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.isValidPackageName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Shnurenko
 */
public class JavaUtilsTest {

    private static final List<String> VALID_PACKAGE_NAMES = Arrays.asList("test.test.test", "_test.test.test", "test_.test.test",
                                                                          "test._test.test", "test.te111st.test", "tes111t.test.test",
                                                                          "test1111.test.test", "test.test.test1111", "test.test_.test",
                                                                          "test.test_._test", "test.test.test_", "test.test.n111",
                                                                          "test.t11.test", "void1.test.test", "test.switchm.test");

    private static final List<String> INVALID_PACKAGE_NAMES = Arrays.asList(" test.test.test", "test.test. test", "test.",
                                                                            "test.test..test", "test.test.:test", "test.test.test)",
                                                                            "test.test{.test", "test.te[st.test", "tes?t.test.test",
                                                                            "test.test.111", "test.test.111n", "test.test.test/",
                                                                            "test.test.te<st", "test.test.te%st", "test.tes*t.test",
                                                                            "void.test.test", "test.class.test", null, "");

    private static final List<String> VALID_CLASS_NAMES = Arrays.asList("test.test.Test", "Test", "Test1", "Test$Test1", "Test_",
                                                                        "_Test", "Test123", "T1e2s3t4");

    private static final List<String> INVALID_CLASS_NAMES = Arrays.asList("test.test.1Test", "{Test", "Te(st1", "Test$Tes}t1", "Te[st_",
                                                                          "_Test.", ".Test123", "T1e-2s3t4", "-T1e-2s3t4", null, "");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void matcherShouldBeCheckedForValidPackageNames() {
        for (String name : VALID_PACKAGE_NAMES) {
            assertTrue("expected: true, but actual: false. Test fails for package name: " + name, isValidPackageName(name));
        }
    }

    @Test
    public void matcherShouldBeCheckedForInValidPackageNames() {
        for (String name : INVALID_PACKAGE_NAMES) {
            assertFalse("test fails for package name: " + name, isValidPackageName(name));
        }
    }

    @Test
    public void illegalStateExceptionShouldBeThrownWhenPackageHasInvalidName() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(containsString("Value is not valid."));

        checkPackageName(".test.test");
    }

    @Test
    public void matcherShouldBeCheckedForValidClassNames() {
        for (String name : VALID_CLASS_NAMES) {
            assertTrue("test fails for class name: " + name, isValidCompilationUnitName(name));
        }
    }

    @Test
    public void matcherShouldBeCheckedForInValidClassNames() {
        for (String name : INVALID_CLASS_NAMES) {
            assertFalse("test fails for class name: " + name, isValidCompilationUnitName(name));
        }
    }

    @Test
    public void illegalStateExceptionShouldBeThrownWhenClassHasInvalidName() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(containsString("Value is not valid."));

        checkCompilationUnitName("1Test");
    }
}
