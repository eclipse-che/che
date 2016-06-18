package org.eclipse.che.ide.ext.java.testing.server;


import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestRunnerFactory {

    private static final String JUNIT4X_RUNNER_CLASS = "org.junit.runner.JUnitCore";
    private static final String JUNIT3X_RUNNER_CLASS = "junit.textui.TestRunner";


    public TestRunner getTestRunner(String projectPath) throws IOException, InterruptedException {

        buildClasspath(projectPath);
        List<URL> classUrls = getProjectClasspath(projectPath);
        ClassLoader classLoader = new URLClassLoader(classUrls.toArray(new URL[classUrls.size()]),null);
        TestRunnerFramework framework = getFramework(classLoader);

        switch (framework){
            case JUNIT3x:
                break;
            case JUNIT4x:
                return null;
            case TESTNG:
                break;
            default:
                break;
        }
        return null;
    }

    private TestRunnerFramework getFramework(ClassLoader projectClassLoader){
        try {
            Class.forName(JUNIT4X_RUNNER_CLASS, true, projectClassLoader);
            return TestRunnerFramework.JUNIT4x;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName(JUNIT3X_RUNNER_CLASS, true, projectClassLoader);
            return TestRunnerFramework.JUNIT3x;
        } catch (ClassNotFoundException ignored) {
        }

        return TestRunnerFramework.UNKNOWN;
    }


    private boolean buildClasspath(String projectPath) throws IOException, InterruptedException {

        final CommandLine commandLineClassPath = new CommandLine("mvn","clean", "dependency:build-classpath",
                "-Dmdep.outputFile=target/test.classpath.maven");
        Process processBuildClassPath = new ProcessBuilder()
                .redirectErrorStream(true)
                .directory(new File(projectPath))
                .command(commandLineClassPath.toShellCommand())
                .start();
        ProcessUtil.process(processBuildClassPath, LineConsumer.DEV_NULL, LineConsumer.DEV_NULL);
        processBuildClassPath.waitFor();

        final CommandLine commandLineTestCompile = new CommandLine("mvn", "test-compile");
        Process processTestCompile = new ProcessBuilder()
                .redirectErrorStream(true)
                .directory(new File(projectPath))
                .command(commandLineTestCompile.toShellCommand())
                .start();
        ProcessUtil.process(processTestCompile, LineConsumer.DEV_NULL, LineConsumer.DEV_NULL);
        return processTestCompile.waitFor() == 0;

    }

    private List<URL> getProjectClasspath(String projectPath) throws IOException {

        List<URL> classUrls = new ArrayList<>();
        File cpFile = Paths.get(projectPath, "target", "test.classpath.maven").toFile();

        FileReader fileReader = new FileReader(cpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = bufferedReader.readLine();
        String[] paths = line.split(":");

        for (String path : paths) {
            classUrls.add(new File(path).toURI().toURL());
        }
        bufferedReader.close();
        fileReader.close();

        classUrls.add(Paths.get(projectPath, "target", "classes").toUri().toURL());
        classUrls.add(Paths.get(projectPath, "target", "test-classes").toUri().toURL());

        return classUrls;
    }

    public ClassLoader getProjectClassLoader(String propath) throws IOException, InterruptedException {
        buildClasspath(propath);
        List<URL> classUrls = getProjectClasspath(propath);
        ClassLoader classLoader = new URLClassLoader(classUrls.toArray(new URL[classUrls.size()]),null);
        return classLoader;
    }
}
