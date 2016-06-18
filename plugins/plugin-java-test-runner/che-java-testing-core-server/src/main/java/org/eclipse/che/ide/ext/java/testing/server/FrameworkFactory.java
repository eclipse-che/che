package org.eclipse.che.ide.ext.java.testing.server;

import com.google.inject.Inject;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class FrameworkFactory {

    private final Map<String,TestRunner2> frameworks = new HashMap<>();


    @Inject
    public FrameworkFactory(Set<TestRunner2> runners) {
        System.out.println("inititilaized FrameworkFactory " + runners.size()+ " fefef " + runners.toString());

        runners.forEach(this::register);
    }

    public void register(@NotNull TestRunner2 handler) {
        frameworks.put(handler.getName(),handler);
    }
    public TestRunner2 getTestRunner(String key){
        return frameworks.get(key);
    }

//    public void addTestRunner(String name,TestRunner2 a){
//            frameworks.put(name,a);
//    }
}
