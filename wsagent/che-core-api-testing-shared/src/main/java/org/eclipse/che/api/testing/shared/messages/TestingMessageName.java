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
package org.eclipse.che.api.testing.shared.messages;

/**
 *
 */
public enum TestingMessageName {
    MESSAGE("message"),
    ROOT_PRESENTATION("rootName"),
    TEST_SUITE_STARTED("testSuiteStarted"),
    TEST_SUITE_FINISHED("testSuiteFinished"),
    TEST_STARTED("testStarted"),
    TEST_FINISHED("testFinished"),
    TEST_IGNORED("testIgnored"),
    TEST_STD_OUT("testStdOut"),
    TEST_STD_ERR("testStdErr"),
    TEST_FAILED("testFailed"),
    TEST_COUNT("testCount"),
    TEST_REPORTER_ATTACHED("testReporterAttached"),
    SUITE_TREE_STARTED("suiteTreeStarted"),
    SUITE_TREE_ENDED("suiteTreeEnded"),
    SUITE_TREE_NODE("suiteTreeNode"),
    BUILD_TREE_ENDED("treeEnded"),
    TESTING_STARTED("testingStarted"),
    FINISH_TESTING("finishTesting"),
    UNCAPTURED_OUTPUT("uncapturedOutput");


    private String name;
    TestingMessageName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TestingMessageName instanceOf(String name) {
        for (TestingMessageName messageName : TestingMessageName.values()) {
            if (messageName.name.equals(name)) {
                return messageName;
            }
        }
        return null;
    }
}
