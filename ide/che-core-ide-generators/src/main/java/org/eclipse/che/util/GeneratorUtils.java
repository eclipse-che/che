/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> */
public class GeneratorUtils {

    /** CLI Argument */
    public static final String  ROOT_DIR_PARAMETER   = "--rootDir=";
    /** Reg Exp that matches the package declaration */
    public static final Pattern PACKAGE_PATTERN      = Pattern
            .compile(".*package\\s+([a-zA_Z_][\\.\\w]*);.*", Pattern.DOTALL);
    /** Current Package name, used to avoid miss-hits of Extension's lookup */
    static final        String  COM_CODENVY_IDE_UTIL = "org.eclipse.che.ide.util";
    public static final String  TAB                  = "   ";
    public static final String  TAB2                 = TAB + TAB;

    /**
     * Extracts Package declaration from file
     *
     * @param fileName
     * @param content
     * @return
     * @throws IOException
     */
    public static String getClassFQN(String fileName, String content) throws IOException {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
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
     * Generates root dir
     *
     * @param args
     * @return File
     */
    public static File getRootFolder(String[] args) {
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
}
