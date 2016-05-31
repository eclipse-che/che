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
package org.eclipse.che.plugin.docker.client.params;

import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Arguments holder for {@link org.eclipse.che.plugin.docker.client.DockerConnector#removeImage(RemoveImageParams)}.
 *
 * @author Mykola Morhun
 */
public class RemoveImageParams {

    private String  image;
    private Boolean force;

    /**
     * Creates arguments holder with required parameters.
     *
     * @param image
     *         image identifier, either id or name
     * @return arguments holder with required parameters
     * @throws NullPointerException
     *         if {@code image} is null
     */
    public static RemoveImageParams create(@NotNull String image) {
        return new RemoveImageParams().withImage(image);
    }

    private RemoveImageParams() {}

    /**
     * Adds image to this parameters.
     *
     * @param image
     *         image identifier, either id or name
     * @return this params instance
     * @throws NullPointerException
     *         if {@code image} is null
     */
    public RemoveImageParams withImage(@NotNull String image) {
        requireNonNull(image);
        this.image = image;
        return this;
    }

    /**
     * Adds force flag to this parameters.
     *
     * @param force
     *         {@code true} means remove an image anyway, despite using of this image
     * @return this params instance
     */
    public RemoveImageParams withForce(boolean force) {
        this.force = force;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Boolean isForce() {
        return force;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoveImageParams that = (RemoveImageParams)o;
        return Objects.equals(image, that.image) &&
               Objects.equals(force, that.force);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, force);
    }

}
