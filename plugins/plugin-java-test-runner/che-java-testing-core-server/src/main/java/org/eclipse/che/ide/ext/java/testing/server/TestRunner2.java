package org.eclipse.che.ide.ext.java.testing.server;

import org.eclipse.che.ide.ext.java.testing.shared.TestResult;

public interface TestRunner2 {

    TestResult execute(String path, ClassLoader classLoader);

    String getName();
}
