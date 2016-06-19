package org.eclipse.che.ide.ext.java.testing.core.server.classpath;

public interface TestClasspathProvider {

    ClassLoader getClassLoader(String projectPath, boolean updateClasspath);

    String getProjectType();
}
