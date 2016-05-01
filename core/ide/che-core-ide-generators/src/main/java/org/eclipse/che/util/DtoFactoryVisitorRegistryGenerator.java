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
package org.eclipse.che.util;

import org.eclipse.che.ide.dto.ClientDtoFactoryVisitor;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.*;

/**
 * Generates {DtoFactoryVisitorRegistry} class source.
 *
 * @author Artem Zatsarynnyi
 */
public class DtoFactoryVisitorRegistryGenerator {

    /**
     * Path of the output class, it definitely should already exits. To ensure proper config.
     * File content will be overridden.
     */
    protected static final String              REGISTRY_PATH      =
            "org/eclipse/che/ide/client/DtoFactoryVisitorRegistry.java";
    /** Map containing <FullFQN, ClassName> */
    protected static final Map<String, String> dtoFactoryVisitors = new HashMap<>();

    /**
     * Entry point. --rootDir is the optional parameter.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            File rootFolder = GeneratorUtils.generateRootFolder(args);
            System.out.println(" ------------------------------------------------------------------------ ");
            System.out.println("Searching for DTO");
            System.out.println(" ------------------------------------------------------------------------ ");

            // find all DtoFactoryVisitors
            findDtoFactoryVisitors();
            GeneratorUtils.generateClassSource(rootFolder, REGISTRY_PATH, dtoFactoryVisitors);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // error =
            System.exit(1);//NOSONAR
        }
    }

    /**
     * Find all the Java classes that have proper @ClientDtoFactoryVisitor annotation.
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private static void findDtoFactoryVisitors() throws IOException {
        Reflections reflection = new Reflections(getConfigurationBuilder());
        Set<Class<?>> classes = reflection.getTypesAnnotatedWith(ClientDtoFactoryVisitor.class);
        int i = 0;
        for (Class clazz : classes) {
            dtoFactoryVisitors.put(clazz.getCanonicalName(), "provider_" + i++);
            System.out.println(String.format("New DtoFactoryVisitor found: %s", clazz.getCanonicalName()));
        }
        System.out.println(String.format("Found: %d DtoFactoryVisitor(s)", dtoFactoryVisitors.size()));
    }
}
