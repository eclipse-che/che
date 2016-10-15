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
package org.eclipse.che.ide.extension.machine.client.targets.categories.docker;

import org.eclipse.che.ide.extension.machine.client.targets.BaseTarget;
import org.eclipse.che.ide.extension.machine.client.targets.Target;

import java.util.Objects;

/**
 * The implementation of {@link Target}.
 *
 * @author Oleksii Orel
 */
public class DockerMachineTarget  extends BaseTarget {
    private String type;
    private String owner;
    private String sourceType;
    private String source;
    private String sourceContent;


    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public String getSourceContent() {
        return sourceContent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DockerMachineTarget)) {
            return false;
        }

        DockerMachineTarget other = (DockerMachineTarget)o;

        return Objects.equals(getName(), other.getName())
               && Objects.equals(getCategory(), other.getCategory())
               && Objects.equals(getRecipe(), other.getRecipe())
               && Objects.equals(getType(), other.getType())
               && Objects.equals(getOwner(), other.getOwner())
               && Objects.equals(getSourceType(), other.getSourceType())
               && Objects.equals(getSourceContent(), other.getSourceContent())
               && Objects.equals(getSource(), other.getSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCategory(), getRecipe(), getType(), getOwner(), getSourceType(), getSource(), getSourceContent());
    }
}
