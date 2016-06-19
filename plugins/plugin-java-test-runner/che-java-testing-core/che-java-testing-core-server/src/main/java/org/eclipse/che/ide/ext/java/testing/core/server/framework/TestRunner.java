package org.eclipse.che.ide.ext.java.testing.core.server.framework;

import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;

import java.util.Map;

public interface TestRunner {

    TestResult execute(Map<String, String> testParameters, TestClasspathProvider classpathProvider);

    String getName();
}
