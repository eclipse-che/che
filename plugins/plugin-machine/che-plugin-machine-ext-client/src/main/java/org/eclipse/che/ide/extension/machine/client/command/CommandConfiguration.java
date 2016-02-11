/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.command;

import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract command which can be configured and executed in machine.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class CommandConfiguration {

    private final CommandType         type;
    private       String              name;
    private       Map<String, String> attributes;

    /**
     * Creates new command configuration of the specified type with the given name.
     *
     * @param type
     *         type of the command
     * @param name
     *         command name
     */
    protected CommandConfiguration(@NotNull CommandType type, @NotNull String name, @Nullable Map<String, String> attributes) {
        this.type = type;
        this.name = name;
        this.attributes = attributes;
    }

    /** Returns command configuration name. */
    @NotNull
    public String getName() {
        return name;
    }

    /** Sets command configuration name. */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /** Returns command configuration type. */
    @NotNull
    public CommandType getType() {
        return type;
    }

    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /** Returns command line to execute in machine. */
    @NotNull
    public abstract String toCommandLine();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CommandConfiguration)) {
            return false;
        }

        CommandConfiguration other = (CommandConfiguration)o;

        return Objects.equals(getName(), other.getName())
               && Objects.equals(getType().getId(), other.getType().getId())
               && Objects.equals(toCommandLine(), other.toCommandLine())
               && Objects.equals(getAttributes(), other.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType().getId(), toCommandLine(), getAttributes());
    }

}
