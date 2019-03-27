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
package org.eclipse.che.api.devfile.server;

import java.util.HashMap;
import org.eclipse.che.api.devfile.model.Devfile;

/** @author Sergii Leshchenko */
public class DevfileFactory {

  private DevfileFactory() {}

  /**
   * Devfile instances should be created with this method.
   *
   * @return empty devfile instance
   */
  public static Devfile newDevfile() {
    Devfile devfile = new Devfile();
    initializeMaps(devfile);
    return devfile;
  }

  /**
   * POJOs are more convenient to use if their collections (including maps) are initialized by
   * default. It allows to adding additional check on null, and initialization before putting first
   * element. Our DTOs and POJOs in other APIs are implemented in this way. But, unfortunately
   * Devfile uses POJOs generator that can't initialize maps by default, see
   * https://github.com/joelittlejohn/jsonschema2pojo/issues/955. It's why we need to do it
   * manually.
   *
   * @param devfile devfile where all maps (including nested objects) should be initialized
   */
  public static void initializeMaps(Devfile devfile) {
    devfile
        .getCommands()
        .stream()
        .filter(command -> command.getAttributes() == null)
        .forEach(command -> command.setAttributes(new HashMap<>()));
    devfile
        .getComponents()
        .stream()
        .filter(component -> component.getSelector() == null)
        .forEach(command -> command.setSelector(new HashMap<>()));
    devfile
        .getComponents()
        .stream()
        .flatMap(component -> component.getEndpoints().stream())
        .filter(endpoint -> endpoint.getAttributes() == null)
        .forEach(endpoint -> endpoint.setAttributes(new HashMap<>()));
  }
}
