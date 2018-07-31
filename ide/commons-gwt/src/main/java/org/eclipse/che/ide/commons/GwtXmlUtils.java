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
package org.eclipse.che.ide.commons;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

/**
 * A smattering of useful methods to work with GWT module descriptor (*.gwt.xml) files.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtXmlUtils {
  /** Filename suffix used for GWT module XML files. */
  public static final String GWT_MODULE_XML_SUFFIX = ".gwt.xml";

  private GwtXmlUtils() {}

  /**
   * Inherit the specified module name in the provided GWT module descriptor.
   *
   * @param path GWT module descriptor
   * @param inheritableModuleLogicalName logical name of the GWT module to inherit
   * @throws java.io.IOException error occurred while reading or writing content of file
   */
  public static void inheritGwtModule(Path path, String inheritableModuleLogicalName)
      throws IOException {
    final String inheritsString = "    <inherits name='" + inheritableModuleLogicalName + "'/>";
    List<String> content = Files.readAllLines(path, UTF_8);
    // insert custom module as last 'inherits' entry
    int i = 0, lastInheritsLine = 0;
    for (String str : content) {
      i++;
      if (str.contains("<inherits")) {
        lastInheritsLine = i;
      }
    }
    content.add(lastInheritsLine, inheritsString);
    Files.write(path, content, UTF_8);
  }

  /**
   * Returns logical name of the first found GWT module.
   *
   * @param folder path to folder that contains project sources
   * @return GWT module logical name
   * @throws java.io.IOException if an I/O error is thrown while finding GWT module descriptor
   * @throws IllegalArgumentException if GWT module descriptor not found
   */
  public static String detectGwtModuleLogicalName(Path folder) throws IOException {
    final String resourcesDir = folder.toString();

    Finder finder = new Finder("*" + GWT_MODULE_XML_SUFFIX);
    Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, finder);
    if (finder.getFirstMatchedFile() == null) {
      throw new IllegalArgumentException("GWT module descriptor (*.gwt.xml) not found.");
    }

    String filePath = finder.getFirstMatchedFile().toString();
    filePath =
        filePath.substring(
            filePath.indexOf(resourcesDir) + resourcesDir.length() + 1,
            filePath.length() - GWT_MODULE_XML_SUFFIX.length());
    return filePath.replace(File.separatorChar, '.');
  }

  /** A {@code FileVisitor} that finds first file that match the specified pattern. */
  private static class Finder extends SimpleFileVisitor<Path> {
    final PathMatcher matcher;
    Path firstMatchedFile;

    Finder(String pattern) {
      matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    /** {@inheritDoc} */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Path fileName = file.getFileName();
      if (fileName != null && matcher.matches(fileName)) {
        firstMatchedFile = file;
        return TERMINATE;
      }
      return CONTINUE;
    }

    /** Returns the first matched {@link java.nio.file.Path}. */
    Path getFirstMatchedFile() {
      return firstMatchedFile;
    }
  }
}
