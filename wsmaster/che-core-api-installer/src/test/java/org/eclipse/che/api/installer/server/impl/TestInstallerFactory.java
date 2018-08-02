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
package org.eclipse.che.api.installer.server.impl;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.model.impl.InstallerServerConfigImpl;
import org.eclipse.che.commons.lang.NameGenerator;

/** @author Anatolii Bazko */
public class TestInstallerFactory {
  public static InstallerImpl createInstaller(String id, String version) {
    return new InstallerImpl(
        (id),
        generate("name"),
        version,
        generate("desc"),
        singletonList(generate("dep")),
        singletonMap(generate("prop"), generate("value")),
        generate("script"),
        singletonMap(
            generate("server"),
            new InstallerServerConfigImpl(
                generate("port"),
                generate("protocol"),
                generate("path"),
                singletonMap(generate("attr"), generate("value")))));
  }

  private static String generate(String prefix) {
    return NameGenerator.generate(prefix, 2);
  }
}
