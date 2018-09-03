/*
 * Copyright (c) 2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import java.util.stream.Stream;
import org.junit.Assert;

/**
 * Base class for JUnit tests of stacktrace line detection.
 *
 * @author Victor Rubezhny
 */
public class BaseOutputCustomizerTest {

  private OutputCustomizer outputCustomizer;
  private OutputCustomizer[] customizers;

  /**
   * Sets the customizer to be tested and array of all the customizers. Normally is to be called
   * from setUp() method, before any test case run.
   *
   * @param customizer
   * @param customizers
   */
  protected void setupTestCustomizers(
      OutputCustomizer customizer, OutputCustomizer... customizers) {
    Assert.assertNotNull("At least one test customizer is to be specified", customizer);
    Assert.assertNotNull("At least one customizer is to be specified", customizers);
    Assert.assertTrue("At least one customizer is to be specified", customizers.length > 0);

    this.outputCustomizer = customizer;
    this.customizers = customizers;
  }

  /**
   * Tests non-customizable lines (those that cannot be treated as stacktrace lines and, as such,
   * shouldn't be customized)
   *
   * @param line
   * @throws Exception
   */
  protected void testStackTraceLine(String line) throws Exception {
    testStackTraceLine(null, line, null);
  }

  /**
   * Tests customizable and non-customizable lines
   *
   * @param expectedCustomizerClass
   * @param line
   * @param expectedCustomization
   * @throws Exception
   */
  protected void testStackTraceLine(
      Class expectedCustomizerClass, String line, String expectedCustomization) throws Exception {
    boolean shouldBeCustomizable = expectedCustomizerClass != null;
    Assert.assertEquals(
        "Line ["
            + line
            + "] is "
            + (shouldBeCustomizable ? "" : "not ")
            + "customizable while it should"
            + (shouldBeCustomizable ? "n\'t " : " ")
            + "be: ",
        Boolean.valueOf(shouldBeCustomizable),
        Boolean.valueOf(outputCustomizer.canCustomize(line)));
    if (shouldBeCustomizable) {
      testCustomizerValidity(expectedCustomizerClass, customizers, line);
      Assert.assertEquals(
          "Wrong customization result:", expectedCustomization, outputCustomizer.customize(line));
    }
  }

  /*
   * Tests that a line can be processed only by expected customizer and not by the
   * other ones
   */
  private void testCustomizerValidity(
      Class expectedCustomizerClass, OutputCustomizer[] allCustomizers, String text) {
    Assert.assertTrue(
        "Expected customizer of type ["
            + expectedCustomizerClass
            + "] cannot customize line ["
            + text
            + "]",
        Stream.of(allCustomizers)
            .filter(child -> child.getClass().equals(expectedCustomizerClass))
            .allMatch(expected -> expected.canCustomize(text)));

    Assert.assertTrue(
        "Unexpected customizer of type ["
            + expectedCustomizerClass
            + "] can customize line ["
            + text
            + "]",
        Stream.of(allCustomizers)
            .filter(child -> !child.getClass().equals(expectedCustomizerClass))
            .noneMatch(expected -> expected.canCustomize(text)));
  }
}
