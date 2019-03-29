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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.Project;

/** @author Sergii Leshchenko */
public class DevfileImpl implements Devfile {

  public DevfileImpl(Devfile devfile) {}

  @Override
  public String getSpecVersion() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public List<? extends Project> getProjects() {
    return null;
  }

  @Override
  public List<? extends Component> getComponents() {
    return null;
  }

  @Override
  public List<? extends Command> getCommands() {
    return null;
  }

  @Override
  public Map<String, String> getAttributes() {
    return null;
  }
}
