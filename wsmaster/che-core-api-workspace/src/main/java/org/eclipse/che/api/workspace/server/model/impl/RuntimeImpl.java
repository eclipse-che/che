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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;

/**
 * Data object for {@link Runtime}.
 *
 * @author Yevhenii Voevodin
 */
public class RuntimeImpl implements Runtime {

  private final String activeEnv;
  private final String owner;
  private final Map<String, ? extends Machine> machines;
  private List<WarningImpl> warnings;

  public RuntimeImpl(String activeEnv, Map<String, ? extends Machine> machines, String owner) {
    this.activeEnv = activeEnv;
    this.machines = machines;
    this.owner = owner;
  }

  public RuntimeImpl(
      String activeEnv,
      Map<String, ? extends Machine> machines,
      String owner,
      List<? extends Warning> warnings) {
    this.activeEnv = activeEnv;
    this.machines = machines;
    this.owner = owner;
    this.warnings = warnings.stream().map(WarningImpl::new).collect(Collectors.toList());
  }

  public RuntimeImpl(Runtime runtime) {
    this.activeEnv = runtime.getActiveEnv();
    this.machines =
        runtime
            .getMachines()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new MachineImpl(e.getValue())));
    this.owner = runtime.getOwner();
    this.warnings =
        runtime.getWarnings().stream().map(WarningImpl::new).collect(Collectors.toList());
  }

  @Override
  public String getActiveEnv() {
    return activeEnv;
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public List<WarningImpl> getWarnings() {
    if (warnings == null) {
      warnings = new ArrayList<>();
    }
    return warnings;
  }

  @Override
  public Map<String, ? extends Machine> getMachines() {
    return machines;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RuntimeImpl)) return false;
    RuntimeImpl that = (RuntimeImpl) o;
    return Objects.equals(activeEnv, that.activeEnv)
        && Objects.equals(machines, that.machines)
        && Objects.equals(owner, that.owner)
        && Objects.equals(warnings, that.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activeEnv, machines, owner, warnings);
  }

  @Override
  public String toString() {
    return "RuntimeImpl{"
        + "activeEnv='"
        + activeEnv
        + '\''
        + ", owner='"
        + owner
        + '\''
        + ", machines="
        + machines
        + ", warnings="
        + warnings
        + '}';
  }
}
