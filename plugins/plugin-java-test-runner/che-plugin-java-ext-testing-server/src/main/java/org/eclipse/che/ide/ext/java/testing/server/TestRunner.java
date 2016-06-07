package org.eclipse.che.ide.ext.java.testing.server;

import org.eclipse.che.ide.ext.java.testing.shared.TestResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class TestRunner {

    protected final String projectPath;
    private final String sourceClassDir = "target/classes";
    private final String testClassDir = "target/test-classes";
    private final String classPathFile = "target/classpath.txt";
    protected final List<URL> classUrls = new ArrayList<URL>();
    protected final ClassLoader projectClassLoader;
    public TestRunner(String projectPath, ClassLoader projectClassLoader) {

        this.projectPath = projectPath;
        this.projectClassLoader = projectClassLoader;
//        this.compile();
//        this.processClasspath();
//        this.addProjectClassPath();
    }


    public abstract TestResult run(String testClass) throws Exception;

    public abstract TestResult runAll() throws Exception;


}
