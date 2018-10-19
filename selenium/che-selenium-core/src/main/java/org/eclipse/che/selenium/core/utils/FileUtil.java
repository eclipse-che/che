/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.utils;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** @author Dmytro Nochevnov */
public class FileUtil {

  /**
   * Checks if directoryToRemove is empty itself and remove it if it is empty only.
   *
   * @param directoryToRemove directory which is verified for emptiness
   * @throws IOException
   */
  public static void removeDirectoryIfItIsEmpty(Path directoryToRemove) throws IOException {
    if (Files.exists(directoryToRemove)
        && Files.isDirectory(directoryToRemove)
        && Files.list(directoryToRemove).collect(toList()).isEmpty()) {

      Files.delete(directoryToRemove);
    }
  }

  public static List<String> readFile(Path pathToFile) throws IOException {
    return Files.readAllLines(pathToFile, StandardCharsets.UTF_8);
  }

  public static List<String> readFile(String pathToFile) throws IOException {
    Path filePath = Paths.get(URI.create(pathToFile));

    return readFile(filePath);
  }

  public static List<String> readFile(URL pathToFile) throws IOException, URISyntaxException {
    Path filePath = Paths.get(pathToFile.toURI());

    return readFile(filePath);
  }

  public static List<String> readFile(URI pathToFile) throws IOException, URISyntaxException {
    Path filePath = Paths.get(pathToFile);

    return readFile(filePath);
  }

  public static String readFileToString(String pathToFile) throws IOException {
    List<String> textFromFile = readFile(pathToFile);
    return Joiner.on('\n').join(textFromFile);
  }

  public static String readFileToString(Path pathToFile) throws IOException {
    List<String> textFromFile = readFile(pathToFile);
    return Joiner.on('\n').join(textFromFile);
  }

  public static String readFileToString(URL pathToFile) throws IOException, URISyntaxException {
    List<String> textFromFile = readFile(pathToFile);
    return Joiner.on('\n').join(textFromFile);
  }

  public static String readFileToString(URI pathToFile) throws IOException, URISyntaxException {
    List<String> textFromFile = readFile(pathToFile);
    return Joiner.on('\n').join(textFromFile);
  }
}
