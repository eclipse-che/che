/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.factory.model;

import java.util.Objects;
import org.eclipse.che.api.core.model.factory.Author;
import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.Policies;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.ide.api.workspace.model.WorkspaceConfigImpl;

/** Data object for {@link Factory}. */
public class FactoryImpl implements Factory {

  private String id;
  private String name;
  private String version;
  private WorkspaceConfigImpl workspace;
  private AuthorImpl creator;
  private ButtonImpl button;
  private IdeImpl ide;
  private PoliciesImpl policies;

  public FactoryImpl(
      String id,
      String name,
      String version,
      WorkspaceConfig workspace,
      Author creator,
      Policies policies,
      Ide ide,
      Button button) {
    this.id = id;
    this.name = name;
    this.version = version;
    if (workspace != null) {
      this.workspace = new WorkspaceConfigImpl(workspace);
    }
    if (creator != null) {
      this.creator = new AuthorImpl(creator);
    }
    if (policies != null) {
      this.policies = new PoliciesImpl(policies);
    }
    if (ide != null) {
      this.ide = new IdeImpl(ide);
    }
    if (button != null) {
      this.button = new ButtonImpl(button);
    }
  }

  public FactoryImpl(Factory factory) {
    this(
        factory.getId(),
        factory.getName(),
        factory.getV(),
        factory.getWorkspace(),
        factory.getCreator(),
        factory.getPolicies(),
        factory.getIde(),
        factory.getButton());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getV() {
    return version;
  }

  @Override
  public WorkspaceConfigImpl getWorkspace() {
    return workspace;
  }

  @Override
  public AuthorImpl getCreator() {
    return creator;
  }

  @Override
  public PoliciesImpl getPolicies() {
    return policies;
  }

  @Override
  public ButtonImpl getButton() {
    return button;
  }

  @Override
  public IdeImpl getIde() {
    return ide;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof FactoryImpl)) return false;
    final FactoryImpl other = (FactoryImpl) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(name, other.name)
        && Objects.equals(version, other.version)
        && Objects.equals(workspace, other.workspace)
        && Objects.equals(creator, other.creator)
        && Objects.equals(policies, other.policies)
        && Objects.equals(ide, other.ide)
        && Objects.equals(button, other.button);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(version);
    hash = 31 * hash + Objects.hashCode(workspace);
    hash = 31 * hash + Objects.hashCode(creator);
    hash = 31 * hash + Objects.hashCode(policies);
    hash = 31 * hash + Objects.hashCode(ide);
    hash = 31 * hash + Objects.hashCode(button);
    return hash;
  }

  @Override
  public String toString() {
    return "FactoryImpl{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", version='"
        + version
        + '\''
        + ", workspace="
        + workspace
        + ", creator="
        + creator
        + ", policies="
        + policies
        + ", ide="
        + ide
        + ", button="
        + button
        + '}';
  }
}
