package org.eclipse.che.ide.ext.java.testing.core.server.classpath;

import com.google.inject.Inject;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class TestClasspathRegistry {

    private final Map<String,TestClasspathProvider> classpathProviders = new HashMap<>();


    @Inject
    public TestClasspathRegistry(Set<TestClasspathProvider> testClasspathProviders) {
        System.out.println("inititilaized TestClasspathRegistry " + testClasspathProviders.size()+ " wkwkwkwkwk " +
                testClasspathProviders.toString());

        testClasspathProviders.forEach(this::register);
    }

    private void register(@NotNull TestClasspathProvider provider) {
        classpathProviders.put(provider.getProjectType(),provider);
    }

    public TestClasspathProvider getTestClasspathProvider(String projectType){
        return classpathProviders.get(projectType);
    }

//    public void addTestRunner(String name,TestRunner2 a){
//            frameworks.put(name,a);
//    }
}
