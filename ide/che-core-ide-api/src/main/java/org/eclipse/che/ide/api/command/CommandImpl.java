/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.command;

import static java.util.Collections.unmodifiableSet;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.commons.annotation.Nullable;

/** Data object for {@link Command}. */
public class CommandImpl implements Command {

  private final String typeId;
  private final ApplicableContext context;
  private String name;
  private String commandLine;
  private Map<String, String> attributes;

  /** Creates new {@link CommandImpl} based on the given data. */
  public CommandImpl(Command command, ApplicableContext context) {
    this(
        command.getName(),
        command.getCommandLine(),
        command.getType(),
        new HashMap<>(command.getAttributes()),
        context);
  }

  /** Creates new {@link CommandImpl} based on the provided data. */
  public CommandImpl(String name, String commandLine, String typeId) {
    this(name, commandLine, typeId, new HashMap<>());
  }

  /** Creates copy of the given {@link Command}. */
  public CommandImpl(Command command) {
    this(
        command.getName(),
        command.getCommandLine(),
        command.getType(),
        new HashMap<>(command.getAttributes()));
  }

  /** Creates copy of the given {@code command}. */
  public CommandImpl(CommandImpl command) {
    this(
        command.getName(),
        command.getCommandLine(),
        command.getType(),
        new HashMap<>(command.getAttributes()),
        new ApplicableContext(command.getApplicableContext()));
  }

  /** Creates new {@link CommandImpl} based on the provided data. */
  public CommandImpl(
      String name, String commandLine, String typeId, Map<String, String> attributes) {
    this.name = name;
    this.commandLine = commandLine;
    this.typeId = typeId;
    this.attributes = attributes;
    this.context = new ApplicableContext();
  }

  /** Creates new {@link CommandImpl} based on the provided data. */
  public CommandImpl(
      String name,
      String commandLine,
      String typeId,
      Map<String, String> attributes,
      ApplicableContext context) {
    this.name = name;
    this.commandLine = commandLine;
    this.typeId = typeId;
    this.attributes = attributes;
    this.context = context;
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
  public String getType() {
    return typeId;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /** Returns ID of the command's goal or {@code null} if none. */
  @Nullable
  public String getGoal() {
    return getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);
  }

  /** Sets command's goal ID. */
  public void setGoal(String goalId) {
    getAttributes().put(COMMAND_GOAL_ATTRIBUTE_NAME, goalId);
  }

  /** Returns command's preview URL or {@code null} if none. */
  @Nullable
  public String getPreviewURL() {
    return getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);
  }

  /** Sets command's preview URL. */
  public void setPreviewURL(String previewURL) {
    getAttributes().put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, previewURL);
  }

  /** Returns command's applicable context. */
  public ApplicableContext getApplicableContext() {
    return context;
  }

  /**
   * {@inheritDoc}
   *
   * @see #equalsIgnoreContext(CommandImpl)
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof CommandImpl)) {
      return false;
    }

    CommandImpl other = (CommandImpl) o;

    return Objects.equals(getName(), other.getName())
        && Objects.equals(typeId, other.typeId)
        && Objects.equals(commandLine, other.commandLine)
        && Objects.equals(getAttributes(), other.getAttributes())
        && Objects.equals(getApplicableContext(), other.getApplicableContext());
  }

  /**
   * Compares this {@link CommandImpl} to another {@link CommandImpl}, ignoring applicable context
   * considerations.
   *
   * @param anotherCommand the {@link CommandImpl} to compare this {@link CommandImpl} against
   * @return {@code true} if the argument represents an equivalent {@link CommandImpl} ignoring
   *     applicable context; {@code false} otherwise
   */
  public boolean equalsIgnoreContext(CommandImpl anotherCommand) {
    if (this == anotherCommand) {
      return true;
    }

    return Objects.equals(getName(), anotherCommand.getName())
        && Objects.equals(typeId, anotherCommand.typeId)
        && Objects.equals(commandLine, anotherCommand.commandLine)
        && Objects.equals(getAttributes(), anotherCommand.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, typeId, commandLine, getAttributes(), getApplicableContext());
  }

  /** Defines the context in which command is applicable. */
  public static class ApplicableContext {

    private boolean workspaceApplicable;
    private Set<String> projects;

    /** Creates new {@link ApplicableContext} which is workspace applicable. */
    public ApplicableContext() {
      workspaceApplicable = true;
      projects = new HashSet<>();
    }

    /** Creates new {@link ApplicableContext} which is applicable to the single project only. */
    public ApplicableContext(String projectPath) {
      projects = new HashSet<>();
      projects.add(projectPath);
    }

    /** Creates new {@link ApplicableContext} based on the provided data. */
    public ApplicableContext(boolean workspaceApplicable, Set<String> projects) {
      this.workspaceApplicable = workspaceApplicable;
      this.projects = projects;
    }

    /** Creates copy of the given {@code context}. */
    public ApplicableContext(ApplicableContext context) {
      this(context.isWorkspaceApplicable(), new HashSet<>(context.getApplicableProjects()));
    }

    /**
     * Returns {@code true} if command is applicable to the workspace and {@code false} otherwise.
     */
    public boolean isWorkspaceApplicable() {
      return workspaceApplicable;
    }

    /** Sets whether the command should be applicable to the workspace or not. */
    public void setWorkspaceApplicable(boolean applicable) {
      this.workspaceApplicable = applicable;
    }

    /** Returns <b>immutable</b> list of the paths of the applicable projects. */
    public Set<String> getApplicableProjects() {
      return unmodifiableSet(projects);
    }

    /** Adds applicable project's path. */
    public void addProject(String path) {
      projects.add(path);
    }

    /** Removes applicable project's path. */
    public void removeProject(String path) {
      projects.remove(path);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof ApplicableContext)) {
        return false;
      }

      ApplicableContext other = (ApplicableContext) o;

      return workspaceApplicable == other.workspaceApplicable
          && Objects.equals(projects, other.projects);
    }

    @Override
    public int hashCode() {
      return Objects.hash(workspaceApplicable, projects);
    }
  }
}
