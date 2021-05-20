/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.PreviewUrlImpl;

/**
 * Data object for {@link Command}.
 *
 * @author Serhii Leshchenko
 */
@Entity(name = "KubernetesRuntimeCommand")
@Table(name = "k8s_runtime_command")
public class KubernetesRuntimeCommandImpl implements Command {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "commandline", nullable = false, columnDefinition = "TEXT")
  private String commandLine;

  @Column(name = "type", nullable = false)
  private String type;

  @Embedded private PreviewUrlImpl previewUrl;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "k8s_runtime_command_attributes",
      joinColumns = @JoinColumn(name = "command_id"))
  @MapKeyColumn(name = "name")
  @Column(name = "value", columnDefinition = "TEXT")
  private Map<String, String> attributes;

  public KubernetesRuntimeCommandImpl() {}

  public KubernetesRuntimeCommandImpl(String name, String commandLine, String type) {
    this.name = name;
    this.commandLine = commandLine;
    this.type = type;
  }

  public KubernetesRuntimeCommandImpl(Command command) {
    this.name = command.getName();
    this.commandLine = command.getCommandLine();
    this.type = command.getType();
    if (command.getPreviewUrl() != null) {
      this.previewUrl = new PreviewUrlImpl(command.getPreviewUrl());
    }
    this.attributes = command.getAttributes();
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getCommandLine() {
    return commandLine;
  }

  public void setCommandLine(String commandLine) {
    this.commandLine = commandLine;
  }

  @Override
  public PreviewUrl getPreviewUrl() {
    return previewUrl;
  }

  public void setPreviewUrl(PreviewUrlImpl previewUrl) {
    this.previewUrl = previewUrl;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KubernetesRuntimeCommandImpl that = (KubernetesRuntimeCommandImpl) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(commandLine, that.commandLine)
        && Objects.equals(type, that.type)
        && Objects.equals(previewUrl, that.previewUrl)
        && Objects.equals(attributes, that.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, commandLine, type, previewUrl, attributes);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", KubernetesRuntimeCommandImpl.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("commandLine='" + commandLine + "'")
        .add("type='" + type + "'")
        .add("previewUrl=" + previewUrl)
        .add("attributes=" + attributes)
        .toString();
  }
}
