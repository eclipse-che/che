/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.command;

import org.eclipse.che.api.core.model.machine.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Command that has {@link ApplicableContext}.
 *
 * @author Artem Zatsarynnyi
 * @see ApplicableContext
 */
public class ContextualCommand extends CommandImpl {

    private final ApplicableContext context;

    /**
     * Creates new {@link ContextualCommand} based on the given
     * {@code command} and {@code context}.
     */
    public ContextualCommand(Command command, ApplicableContext context) {
        super(command);

        this.context = context;
    }

    /**
     * Creates new {@link ContextualCommand} based on the given
     * {@code command} with the empty applicable context.
     */
    public ContextualCommand(Command command) {
        super(command);

        context = new ApplicableContext();
    }

    /** Creates copy of the given {@code command}. */
    public ContextualCommand(ContextualCommand command) {
        this(command.getName(),
             command.getCommandLine(),
             command.getType(),
             new HashMap<>(command.getAttributes()),
             new ApplicableContext(command.getApplicableContext()));
    }

    /** Creates new {@link ContextualCommand} based on the provided data. */
    public ContextualCommand(String name,
                             String commandLine,
                             String typeId) {
        super(name, commandLine, typeId);

        this.context = new ApplicableContext(true, emptyList());
    }

    /** Creates new {@link ContextualCommand} based on the provided data. */
    public ContextualCommand(String name,
                             String commandLine,
                             String typeId,
                             Map<String, String> attributes,
                             ApplicableContext context) {
        super(name, commandLine, typeId, attributes);

        this.context = context;
    }

    /** Returns command's applicable context. */
    public ApplicableContext getApplicableContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ContextualCommand)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        ContextualCommand other = (ContextualCommand)o;

        return Objects.equals(getApplicableContext(), other.getApplicableContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), context);
    }

    /** Defines the context in which command is applicable. */
    public static class ApplicableContext {

        private boolean      workspaceApplicable;
        private List<String> projects;

        /** Creates 'empty' applicable context. */
        public ApplicableContext() {
            projects = new ArrayList<>();
        }

        /** Creates new {@link ApplicableContext} based on the provided data. */
        public ApplicableContext(boolean workspaceApplicable, List<String> projects) {
            this.workspaceApplicable = workspaceApplicable;
            this.projects = projects;
        }

        /** Creates copy of the given {@code applicableContext}. */
        public ApplicableContext(ApplicableContext applicableContext) {
            this(applicableContext.isWorkspaceApplicable(), new ArrayList<>(applicableContext.getApplicableProjects()));
        }

        /** Returns {@code true} if command is applicable to the workspace and {@code false} otherwise. */
        public boolean isWorkspaceApplicable() {
            return workspaceApplicable;
        }

        /** Sets whether the command should be applicable to the workspace or not. */
        public void setWorkspaceApplicable(boolean applicable) {
            this.workspaceApplicable = applicable;
        }

        /** Returns <b>immutable</b> list of the paths of the applicable projects. */
        public List<String> getApplicableProjects() {
            return unmodifiableList(projects);
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

            ApplicableContext other = (ApplicableContext)o;

            return workspaceApplicable == other.workspaceApplicable &&
                   Objects.equals(projects, other.projects);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workspaceApplicable, projects);
        }
    }
}
