/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> */
public class GeneratorUtils {

  /** CLI Argument */
  public static final String ROOT_DIR_PARAMETER = "--rootDir=";
  /** Reg Exp that matches the package declaration */
  public static final Pattern PACKAGE_PATTERN =
      Pattern.compile(".*package\\s+([a-zA_Z_][\\.\\w]*);.*", Pattern.DOTALL);
  /** Current Package name, used to avoid miss-hits of Extension's lookup */
  static final String COM_CODENVY_IDE_UTIL = "org.eclipse.che.ide.util";

  public static final String TAB = "   ";
  public static final String TAB2 = TAB + TAB;

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
      throw new IOException(
          String.format(
              "Class %s doesn't seem to be valid. Package declaration is missing.", fileName));
    }
    if (matcher.groupCount() != 1) {
      throw new IOException(
          String.format(
              "Class %s doesn't seem to be valid. Package declaration is missing.", fileName));
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
        System.err.print(
            "Wrong usage. There is only one allowed argument : " + ROOT_DIR_PARAMETER); // NOSONAR
        System.exit(1); // NOSONAR
      }
    }
    return new File(rootDirPath);
  }

  /**
   * Parse command line arguments in format --key=value --key2=value2. Multiple keys is allowed
   *
   * @param args command line argument.
   * @return Mapping of keys to values.
   */
  public static Map<String, Set<String>> parseArgs(String[] args) {
    Map<String, Set<String>> parsedArgs = new HashMap<>();
    for (String arg : args) {
      int index = arg.indexOf("=");
      if (arg.startsWith("--") && index > 0) {
        String argName = arg.substring(2, index);
        parsedArgs.computeIfAbsent(argName, k -> new HashSet<>()).add(arg.substring(index + 1));
      }
    }
    return parsedArgs;
  }
}
