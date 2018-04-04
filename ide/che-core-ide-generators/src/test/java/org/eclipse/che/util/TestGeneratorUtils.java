/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.testng.annotations.Test;

/**
 * @author Nikolay Zamosenchuk
 * @author Sergii Kabashniuk
 */
public class TestGeneratorUtils {

  /** Should match package name */
  @Test
  public void shouldMatchPackage() {
    String packageString = "package org.eclipse.che.ide.util;" + "import junit.framework.Assert;";
    Matcher matcher = GeneratorUtils.PACKAGE_PATTERN.matcher(packageString);
    assertTrue(matcher.matches());
    assertEquals(matcher.groupCount(), 1);
    String group = matcher.group(1);

    assertEquals(group, "org.eclipse.che.ide.util");
  }

  /**
   * Should match package name
   *
   * @throws IOException
   */
  @Test
  public void shouldExtractPackage() throws IOException {
    String packageString = "package org.eclipse.che.ide.util;" + "import junit.framework.Assert;";
    assertEquals(GeneratorUtils.getClassFQN("dummy", packageString), "org.eclipse.che.ide.util");
  }

  @Test
  public void shouldParseRootDir() {
    // given
    String[] args = new String[] {"--rootDir=/tmp/dir"};
    // when
    File actual = GeneratorUtils.getRootFolder(args);
    // then
    assertEquals(actual.getAbsolutePath(), "/tmp/dir");
  }

  @Test
  public void shouldReturnCurrentDirIfNotSet() {
    // given
    String[] args = new String[] {};
    // when
    File actual = GeneratorUtils.getRootFolder(args);
    // then
    assertEquals(actual.getPath(), ".");
  }

  @Test
  public void shouldReturnCurrentPathIfTooManyArguments() {
    // given
    String[] args = new String[] {"--rootDir=/tmp/dir", "--par2=val2"};
    // when
    File actual = GeneratorUtils.getRootFolder(args);
    // then
    assertEquals(actual.getPath(), ".");
  }

  @Test
  public void shouldBeAbleToParseSingleArgument() {
    // given
    String[] args = new String[] {"--rootDir=/tmp/dir"};
    // when
    Map<String, Set<String>> actual = GeneratorUtils.parseArgs(args);
    // then
    assertEquals(actual.size(), 1);
    Set<String> values = actual.get("rootDir");
    assertEquals(values.size(), 1);
    assertEquals(values.iterator().next(), "/tmp/dir");
  }

  @Test
  public void shouldBeAbleToParseTwoArgument() {
    // given
    String[] args = new String[] {"--rootDir=/tmp/dir", "--par2=val2"};
    // when
    Map<String, Set<String>> actual = GeneratorUtils.parseArgs(args);
    // then
    assertEquals(actual.size(), 2);
    Set<String> values = actual.get("rootDir");
    assertEquals(values.size(), 1);
    assertEquals(values.iterator().next(), "/tmp/dir");
  }

  @Test
  public void shouldBeAbleToParseEmptyArgument() {
    // given
    String[] args = new String[] {};
    // when
    Map<String, Set<String>> actual = GeneratorUtils.parseArgs(args);
    // then
    assertEquals(actual.size(), 0);
  }

  @Test
  public void shouldBeAbleToParseMultipleVales() {
    // given
    String[] args = new String[] {"--rootDir=/tmp/dir", "--rootDir=/tmp/dir2", "--par2=val2"};
    // when
    Map<String, Set<String>> actual = GeneratorUtils.parseArgs(args);
    // then
    assertEquals(actual.size(), 2);
    Set<String> values = actual.get("rootDir");
    assertEquals(values.size(), 2);
    assertTrue(values.contains("/tmp/dir"));
    assertTrue(values.contains("/tmp/dir2"));
  }
}
