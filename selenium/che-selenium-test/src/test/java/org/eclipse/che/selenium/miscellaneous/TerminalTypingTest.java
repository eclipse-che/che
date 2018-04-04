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
package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

/** @author Alexander Andrienko */
public class TerminalTypingTest {

  private List<Pair<String, String>> keyPairs =
      Arrays.asList(
          new Pair<>("a", "A"),
          new Pair<>("b", "B"),
          new Pair<>("c", "C"),
          new Pair<>("d", "D"),
          new Pair<>("e", "E"),
          new Pair<>("f", "F"),
          new Pair<>("g", "G"),
          new Pair<>("h", "H"),
          new Pair<>("i", "I"),
          new Pair<>("j", "J"),
          new Pair<>("k", "K"),
          new Pair<>("l", "L"),
          new Pair<>("m", "M"),
          new Pair<>("n", "N"),
          new Pair<>("o", "O"),
          new Pair<>("p", "P"),
          new Pair<>("q", "Q"),
          new Pair<>("r", "R"),
          new Pair<>("s", "S"),
          new Pair<>("t", "T"),
          new Pair<>("u", "U"),
          new Pair<>("v", "V"),
          new Pair<>("w", "W"),
          new Pair<>("x", "X"),
          new Pair<>("v", "V"),
          new Pair<>("z", "Z"),
          new Pair<>("-", "_"),
          new Pair<>("1", "!"),
          new Pair<>("2", "@"),
          new Pair<>("3", "#"),
          new Pair<>("4", "$"),
          new Pair<>("5", "%"),
          new Pair<>("6", "^"),
          new Pair<>("7", "&"),
          new Pair<>("8", "*"),
          new Pair<>("9", "("),
          new Pair<>("0", ")"),
          new Pair<>("=", "+"),
          new Pair<>("[", "{"),
          new Pair<>("]", "}"),
          new Pair<>("'", "\""),
          new Pair<>("\\", "|"),
          new Pair<>(",", "<"),
          new Pair<>(".", ">"),
          new Pair<>("/", "?"),
          new Pair<>("<", "<"),
          new Pair<>("`", "~"));

  @Inject private Loader loader;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private MachineTerminal terminal;
  @Inject private ProjectExplorer projectExplorer;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab();
  }

  @Test
  public void checkTerminalTypingCharsWithoutShift() {
    loader.waitOnClosed();
    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();

    for (Pair<String, String> pair : keyPairs) {
      terminal.typeIntoTerminal(pair.first());
      terminal.waitExpectedTextIntoTerminal("$ " + pair.first());
      terminal.typeIntoTerminal(Keys.BACK_SPACE.toString());
    }
  }

  @Test
  public void checkTerminalTypingWithShift() {
    loader.waitOnClosed();
    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();

    for (Pair<String, String> pair : keyPairs) {
      terminal.typeIntoTerminal(Keys.SHIFT + pair.first());
      terminal.waitExpectedTextIntoTerminal("$ " + pair.second());
      terminal.typeIntoTerminal(Keys.BACK_SPACE.toString());
    }
  }
}
