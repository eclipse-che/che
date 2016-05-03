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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> */
public class GeneratorUtils {

    /** CLI Argument */
    public static final String ROOT_DIR_PARAMETER = "--rootDir=";

    /**
     * Extracts Package declaration from file
     *
     * @param fileName
     * @param content
     * @return
     * @throws IOException
     */
    public static String getClassFQN(String fileName, String content) throws IOException {
        Matcher matcher = GeneratorUtils.PACKAGE_PATTERN.matcher(content);
        if (!matcher.matches()) {
            throw new IOException(String.format("Class %s doesn't seem to be valid. Package declaration is missing.",
                                                fileName));
        }
        if (matcher.groupCount() != 1) {
            throw new IOException(String.format("Class %s doesn't seem to be valid. Package declaration is missing.",
                                                fileName));
        }
        return matcher.group(1);
    }

    /**
     * Generate a root folder
     *
     * @param args
     * @return File
     */
    public static File generateRootFolder(String[] args) {
        String rootDirPath = ".";
        // try to read argument
        if (args.length == 1) {
            if (args[0].startsWith(ROOT_DIR_PARAMETER)) {
                rootDirPath = args[0].substring(ROOT_DIR_PARAMETER.length());
            } else {
                System.err.print("Wrong usage. There is only one allowed argument : "
                        + ROOT_DIR_PARAMETER);//NOSONAR
                System.exit(1);//NOSONAR
            }
        }

        return new File(rootDirPath);
    }

    /**
     * Generate to source of the Class
     *
     * @param rootFolder
     * @param path
     * @param fqn
     * @throws IOException
     */
    public static void generateClassSource(File rootFolder, String path, Map<String, String> fqn) throws IOException {
        File extManager = new File(rootFolder, path);
        StringBuilder builder = new StringBuilder();
        builder.append("package org.eclipse.che.ide.client;\n\n");
        generateImports(builder);
        generateClass(builder, path, fqn);
        // flush content
        FileUtils.writeStringToFile(extManager, builder.toString());
    }

    /**
     * Generate imports.
     *
     * @param builder
     */
    public static void generateImports(StringBuilder builder) {
        builder.append("import com.google.inject.Inject;\n");
        builder.append("import com.google.inject.Provider;\n");
        builder.append("import com.google.inject.Singleton;\n");
        builder.append("import java.util.HashMap;\n");
        builder.append("import java.util.Map;\n");
    }

    /**
     * Generate Class declarations
     *
     * @param builder
     * @param path
     * @param fqn
     */
    public static void generateClass(StringBuilder builder, String path, Map<String, String> fqn) {
        final String className = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));

        String shortClassName = getShortClassName(className);

        // generate class header
        builder.append("/**\n");
        builder.append(" * THIS CLASS WILL BE OVERRIDDEN BY MAVEN BUILD. DON'T EDIT CLASS, IT WILL HAVE NO EFFECT.\n");
        builder.append(" */\n");
        builder.append("@Singleton\n");
        builder.append("@SuppressWarnings(\"rawtypes\")\n");
        builder.append("public class ");
        builder.append(className);
        builder.append("\n");
        builder.append("{\n");
        builder.append("\n");

        // field
        builder.append(GeneratorUtils.TAB);
        builder.append("/** Contains the map with all the ");
        builder.append(shortClassName);
        builder.append(" Providers <FullClassFQN, Provider>. */\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("protected final Map<String, Provider> providers = new HashMap<>();\n\n");

        // generate constructor

        builder.append(GeneratorUtils.TAB);
        builder.append("/** Constructor that accepts all the ");
        builder.append(shortClassName);
        builder.append(" found in IDE package */\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("@Inject\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("public ");
        builder.append(className);
        builder.append("\n");

        // paste args here
        Iterator<Map.Entry<String, String>> entryIterator = fqn.entrySet().iterator();
        while (entryIterator.hasNext()) {
            // <FullFQN, ClassName>
            Map.Entry<String, String> entry = entryIterator.next();
            String hasComma = entryIterator.hasNext() ? "," : "";
            // add constructor argument like:
            // fullFQN classNameToLowerCase,
            String classFQN = String.format("Provider<%s>", entry.getKey());
            String variableName = entry.getValue().toLowerCase();
            builder.append(GeneratorUtils.TAB2);
            builder.append(classFQN);
            builder.append(" ");
            builder.append(variableName);
            builder.append(hasComma);
            builder.append("\n");
        }

        builder.append(GeneratorUtils.TAB);
        builder.append(")\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("{\n");

        // paste add here
        for (Map.Entry<String, String> entry : fqn.entrySet()) {
            String fullFqn = entry.getKey();
            String variableName = entry.getValue().toLowerCase();

            String putStatement = String.format("this.providers.put(\"%s\",%s);%n", fullFqn, variableName);
            builder.append(GeneratorUtils.TAB2);
            builder.append(putStatement);
        }

        // close constructor
        builder.append(GeneratorUtils.TAB);
        builder.append("}\n\n");

        // generate getter
        builder.append(GeneratorUtils.TAB);
        builder.append("/** Returns the map with all the ");
        builder.append(shortClassName);
        builder.append(" Providers <FullClassFQN, Provider>. */\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("public Map<String, Provider> getProviders()\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("{\n");
        builder.append(GeneratorUtils.TAB2);
        builder.append("return providers;\n");
        builder.append(GeneratorUtils.TAB);
        builder.append("}\n");

        // close class
        builder.append("}\n");

    }

    private static String getShortClassName(String className) {
        Pattern pattern = Pattern.compile("[A-Z][^A-Z]*$");
        Matcher match = pattern.matcher(className);
        match.find();
        return className.substring(0, match.start());
    }

    /** Reg Exp that matches the package declaration */
    public static final Pattern PACKAGE_PATTERN      = Pattern
            .compile(".*package\\s+([a-zA_Z_][\\.\\w]*);.*", Pattern.DOTALL);
    /** Current Package name, used to avoid miss-hits of Extension's lookup */
    static final        String  COM_CODENVY_IDE_UTIL = "org.eclipse.che.ide.util";
    public static final String  TAB                  = "   ";
    public static final String  TAB2                 = TAB + TAB;

}
