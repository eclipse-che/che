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

import org.eclipse.che.ide.api.extension.Extension;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.eclipse.che.util.IgnoreUnExistedResourcesReflectionConfigurationBuilder.*;

/**
 * Generates {ExtensionManager} class source
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public class ExtensionManagerGenerator {

    /** Annotation to look for. */
    protected static final String EXT_ANNOTATION = "@Extension";

    /** Reg Exp that matches the "@Extension  ( ... )" */
    protected static final Pattern EXT_PATTERN = Pattern.compile(".*@Extension\\s*\\(.*\\).*", Pattern.DOTALL);

    /**
     * Path of the output class, it definitely should already exits. To ensure proper config.
     * File content will be overridden.
     */
    protected static final String EXT_MANAGER_PATH =
            "/org/eclipse/che/ide/client/ExtensionManager.java";

    /** Map containing <FullFQN, ClassName> */
    protected static final Map<String, String> EXTENSIONS_FQN = new HashMap<>();

    /**
     * Entry point. --rootDir is the optional parameter.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            File rootFolder = GeneratorUtils.generateRootFolder(args);
            System.out.println(" ------------------------------------------------------------------------ ");
            System.out.println(String.format("Searching for Extensions in %s", rootFolder.getAbsolutePath()));
            System.out.println(" ------------------------------------------------------------------------ ");
            // find all Extension FQNs
            findExtensions();
            GeneratorUtils.generateClassSource(rootFolder, EXT_MANAGER_PATH, EXTENSIONS_FQN);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // error
            System.exit(1);//NOSONAR
        }
    }

    /**
     * Find all the Java Classes that have proper @Extension declaration
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void findExtensions() throws IOException {
        Reflections reflection = new Reflections(getConfigurationBuilder());
        Set<Class<?>> classes = reflection.getTypesAnnotatedWith(Extension.class);
        for (Class clazz : classes) {
            EXTENSIONS_FQN.put(clazz.getCanonicalName(), clazz.getSimpleName());
            System.out.println(String.format("New Extension Found: %s", clazz.getCanonicalName()));
        }
        System.out.println(String.format("Found: %d extensions", EXTENSIONS_FQN.size()));
    }
}
