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
package org.eclipse.che.api.core.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/** @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a> */
public class StandardLinuxShellTest {

  @Test
  public void testEscapeSpaces() throws Exception {
    CommandLine cmd = new CommandLine().add("ls", "-l", "/home/andrew/some dir");
    final String[] line = new ShellFactory.StandardLinuxShell().createShellCommand(cmd);
    final String[] expected = {"/bin/bash", "-cl", "ls -l /home/andrew/some\\ dir"};
    Assert.assertEquals(line, expected);
  }

  @Test
  public void testEscapeControls() {
    CommandLine cmd = new CommandLine().add("ls", "-l", "/home/andrew/c|r>a$z\"y'dir&");
    final String[] line = new ShellFactory.StandardLinuxShell().createShellCommand(cmd);
    final String[] expected = {
      "/bin/bash", "-cl", "ls -l /home/andrew/c\\|r\\>a\\$z\\\"y\\'dir\\&"
    };
    Assert.assertEquals(line, expected);
  }

  @Test
  public void testEscapeSpecCharacters() {
    CommandLine cmd = new CommandLine().add("ls", "-l", "/home/andrew/some\n\r\t\b\fdir");
    final String[] line = new ShellFactory.StandardLinuxShell().createShellCommand(cmd);
    final String[] expected = {"/bin/bash", "-cl", "ls -l /home/andrew/some\\n\\r\\t\\b\\fdir"};
    Assert.assertEquals(line, expected);
  }
}
