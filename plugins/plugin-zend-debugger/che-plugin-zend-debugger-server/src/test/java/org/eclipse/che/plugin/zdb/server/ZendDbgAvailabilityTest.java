/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

/**
 * Class responsible for checking if PHP with Zend Debugger is installed in test environment.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgAvailabilityTest {

  private static final String NO_PHP_INSTALLED =
      "PHP installation could not be found in test/OS environment.";
  private static final String NO_ZEND_DEBUGGER_INSTALLED =
      "Zend Debugger extension for PHP is not installed.";
  private static final String TEST_ENVIRONMENT = "\nTest Environment:\n{0}\n{1}\n";

  @Test(groups = {"checkPHP"})
  public void checkPHP() throws Exception {
    List<String> phpInfo = new ArrayList<>();
    try {
      Process p = Runtime.getRuntime().exec(new String[] {"php", "-v"});
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          phpInfo.add(line);
        }
      }
    } catch (Exception e) {
      throw new Exception(NO_PHP_INSTALLED);
    }
    String phpVersionLine = phpInfo.get(0);
    if (!phpVersionLine.startsWith("PHP")) {
      throw new Exception(NO_PHP_INSTALLED);
    }
    String phpZendDebuggerLine = null;
    for (String currentLine : phpInfo) {
      if (currentLine.contains("Zend Debugger")) {
        phpZendDebuggerLine = currentLine;
      }
    }
    if (phpZendDebuggerLine == null) {
      throw new Exception(NO_ZEND_DEBUGGER_INSTALLED);
    }
    System.out.println(MessageFormat.format(TEST_ENVIRONMENT, phpVersionLine, phpZendDebuggerLine));
  }
}
