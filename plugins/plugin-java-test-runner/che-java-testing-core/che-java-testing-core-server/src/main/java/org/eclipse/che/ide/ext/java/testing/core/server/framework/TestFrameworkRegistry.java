package org.eclipse.che.ide.ext.java.testing.core.server.framework;

import com.google.inject.Inject;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class TestFrameworkRegistry {

    private final Map<String,TestRunner> frameworks = new HashMap<>();


    @Inject
    public TestFrameworkRegistry(Set<TestRunner> runners) {
        System.out.println("inititilaized TestFrameworkRegistry " + runners.size()+ " fefef " + runners.toString());

        runners.forEach(this::register);
    }

    private void register(@NotNull TestRunner handler) {
        frameworks.put(handler.getName(),handler);
    }

    public TestRunner getTestRunner(String key){
        return frameworks.get(key);
    }

//    public void addTestRunner(String name,TestRunner2 a){
//            frameworks.put(name,a);
//    }
}
