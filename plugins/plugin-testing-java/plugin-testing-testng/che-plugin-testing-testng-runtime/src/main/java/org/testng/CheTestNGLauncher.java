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
package org.testng;

import com.beust.jcommander.JCommander;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Main entry point which runs TestNG framework. */
public class CheTestNGLauncher {

  public static void main(String[] args) {
    List<String> filteredArgs = new ArrayList<>();

    int i = 0;
    for (; i < args.length; i++) {
      String arg = args[i];

      if (arg.equals("-suiteFile")) {
        break;
      }

      filteredArgs.add(arg);
    }

    filteredArgs.add(args[i + 1]);
    CheTestNG cheTestNG = new CheTestNG();
    CommandLineArgs cla = new CommandLineArgs();
    new JCommander(
        Collections.singletonList(cla), filteredArgs.toArray(new String[filteredArgs.size()]));
    cheTestNG.configure(cla);
    cheTestNG.run();
  }
}
