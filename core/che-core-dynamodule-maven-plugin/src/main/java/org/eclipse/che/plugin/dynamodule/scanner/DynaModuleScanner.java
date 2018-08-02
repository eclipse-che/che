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
package org.eclipse.che.plugin.dynamodule.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.eclipse.che.commons.lang.ZipUtils;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scanner used to scan the directories and file in order to analyze all classes.
 *
 * @author Florent Benoit
 */
public class DynaModuleScanner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DynaModuleScanner.class);

  private List<UrlTime> times = new ArrayList<>();

  /** List of the classes that are containing DynaModules. */
  private Set<String> matchingClasses = new HashSet<>();

  private final List<String> skipResources = new ArrayList<>();

  /** Directory used to unpack war files */
  private File unpackedDirectory;

  /** Scan jars in war. */
  private boolean scanJarInWarDependencies;

  public DynaModuleScanner() {

    // skipping this dependencies
    this.skipResources.add(".*com/google/gwt/gwt-.*/.*/gwt-.*.jar");
    this.skipResources.add(".*com/google/inject/guice.*/.*/guice.*.jar");
    this.skipResources.add(".*org/testng/testng/.*/testng-.*.jar");
    this.skipResources.add(".*org/apache/lucene/lucene-.*/.*/lucene-.*.jar");
    this.skipResources.add(".*com/google/guava/guava/.*/guava-.*.jar");
    this.skipResources.add(
        ".*org/eclipse/che/lib/org-eclipse-jdt-core-repack/.*/org-eclipse-jdt-core-repack-.*.jar");
    this.skipResources.add(
        ".*org/eclipse/che/plugin/org.eclipse.jdt.ui/.*/org.eclipse.jdt.ui-.*.jar");
    this.skipResources.add(".*org/eclipse/lsp4j/org.eclipse.lsp4j/.*/org.eclipse.lsp4j-.*.jar");
    this.skipResources.add(".*org/eclipse/tycho/org.eclipse.osgi/.*/org.eclipse.osgi-.*.jar");
    this.skipResources.add(".*com/fasterxml/jackson/core/.*.jar");
    this.skipResources.add(".*org/eclipse/xtend/.*.jar");
    this.skipResources.add(".*org/eclipse/search/.*.jar");
  }

  /**
   * Public method that is accepting the URL
   *
   * @param url the URL to scan
   * @throws URISyntaxException
   * @throws IOException
   */
  public void scan(URL url) throws URISyntaxException, IOException {

    boolean skip = skipResources.stream().anyMatch(pattern -> url.toString().matches(pattern));
    if (skip) {
      LOGGER.debug("skipping URL {}", url);
      return;
    }

    performScan(url);
  }

  /**
   * Performs the scan of the URL.
   *
   * @param url the url to analyze
   * @throws URISyntaxException
   * @throws IOException
   */
  protected void performScan(URL url) throws URISyntaxException, IOException {

    long start = System.currentTimeMillis();

    // scan is based upon the type of the URL
    Path path = Paths.get(url.toURI());
    if (Files.isDirectory(path)) {
      scanDirectory(path);
    } else {
      if (path.toString().endsWith(".jar") || path.toString().endsWith(".war")) {
        if (scanJarInWarDependencies && path.toString().endsWith(".war")) {
          scanDeepWar(path);
        } else {
          try (JarFile jarFile = new JarFile(path.toFile())) {
            scanJar(jarFile);
          }
        }
      } else if (path.toString().endsWith(".class")) {
        scanFile(path);
      }
    }
    long end = System.currentTimeMillis();
    times.add(new UrlTime(url, (end - start)));
  }

  /** scan the given directory */
  protected void scanDirectory(Path directory) throws IOException {
    final int maxDepth = 10;
    Stream<Path> matches =
        java.nio.file.Files.find(
            directory, maxDepth, (path, basicFileAttributes) -> path.toString().endsWith(".class"));
    matches.forEach(
        file -> {
          try {
            scanFile(file);
          } catch (IOException e) {
            throw new IllegalStateException("Unable to scan the file", e);
          }
        });

    matches =
        java.nio.file.Files.find(
            directory, maxDepth, (path, basicFileAttributes) -> path.toString().endsWith(".jar"));
    matches.forEach(
        file -> {
          boolean skip =
              skipResources.stream().anyMatch(pattern -> file.toString().matches(pattern));
          if (skip) {
            LOGGER.debug("skipping jar file {} inside directory {}", file.toFile(), directory);
          } else {
            try (JarFile jarFile = new JarFile(file.toFile())) {
              scanJar(jarFile);
            } catch (IOException e) {
              throw new IllegalStateException("Unable to scan the file", e);
            }
          }
        });
  }

  /** scan the given .class file */
  protected void scanFile(Path file) throws IOException {
    boolean skip = skipResources.stream().anyMatch(pattern -> file.toString().matches(pattern));
    if (skip) {
      LOGGER.debug("skipping file {}", file);
      return;
    }

    if (Files.isRegularFile(file)) {
      try (InputStream is = new FileInputStream(file.toFile())) {
        scanInputStream(is);
      }
    }
  }

  /** scan the given jar file */
  protected void scanJar(final JarFile jarFile) throws IOException {

    Enumeration<JarEntry> enumEntries = jarFile.entries();
    while (enumEntries.hasMoreElements()) {
      JarEntry jarEntry = enumEntries.nextElement();
      if (jarEntry.getName().endsWith(".class")) {
        boolean skip =
            skipResources.stream().anyMatch(pattern -> jarEntry.getName().matches(pattern));
        if (skip) {
          LOGGER.debug("skipping jar entry from jarFile {}", jarFile);
        } else {
          scanInputStream(jarFile.getInputStream(jarEntry));
        }
      }
    }
  }

  protected void scanDeepWar(final Path warPath) throws IOException {

    // name
    File file = warPath.toFile();
    File unpackedFile = new File(this.unpackedDirectory, file.getName());
    // unpack the war
    ZipUtils.unzip(file, unpackedFile);

    // now scan the directory
    scanDirectory(unpackedFile.toPath());
  }

  /** scan the given inputstream */
  protected void scanInputStream(InputStream inputStream) throws IOException {
    FindDynaModuleVisitor findDynaModuleVisitor = new FindDynaModuleVisitor();
    new ClassReader(inputStream).accept(findDynaModuleVisitor, 0);
    if (findDynaModuleVisitor.isDynaModule()) {
      matchingClasses.add(findDynaModuleVisitor.getClassname());
    }
  }

  /**
   * Gets the list of all classes annotated by {@link org.eclipse.che.inject.DynaModule}
   *
   * @return the list of stringified name of classes
   */
  public Set<String> getDynaModuleClasses() {
    return matchingClasses;
  }

  public void setAdditionalSkipResources(String[] additionalSkipResources) {
    if (additionalSkipResources != null && additionalSkipResources.length > 0) {
      this.skipResources.addAll(Arrays.asList(additionalSkipResources));
    }
  }

  public void setUnpackedDirectory(File unpackedDirectory) {
    this.unpackedDirectory = unpackedDirectory;
  }

  public void setScanJarInWarDependencies(boolean scanJarInWarDependencies) {
    this.scanJarInWarDependencies = scanJarInWarDependencies;
  }

  public static class UrlTime implements Comparable<UrlTime> {

    private final URL url;
    private final long time;

    public UrlTime(URL url, long time) {
      this.url = url;
      this.time = time;
    }

    /** Allow to sort the URLs from max time to analyze to min time. */
    @Override
    public int compareTo(UrlTime o) {
      return Long.valueOf(o.time).compareTo(Long.valueOf(this.time));
    }
  }

  /** Display statistics on the time required to parse the JAR files. */
  public void stats() {
    Collections.sort(times);
    times.forEach(
        urlTime -> LOGGER.debug("Scan of URL {} done in {} ms", urlTime.url, urlTime.time));
  }
}
