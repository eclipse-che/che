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
package org.eclipse.che.api.workspace.server.stack.image;

import com.google.common.base.Objects;

import org.eclipse.che.commons.annotation.Nullable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Arrays;

/**
 * Class for storing {@link org.eclipse.che.api.workspace.shared.stack.Stack} icon data
 *
 * @author Alexander Andrienko
 */
@Embeddable
public class StackIcon {

    @Column(name = "icon_name")
    private String name;

    @Column(name = "mediatype")
    private String mediaType;

    @Column(name = "data")
    private byte[] data;

    public StackIcon() {}

    public StackIcon(String name, String mediaType, @Nullable byte[] data) {
        this.data = data;
        this.mediaType = mediaType;
        this.name = name;
    }

    public StackIcon(StackIcon icon) {
        this(icon.name, icon.mediaType, Arrays.copyOf(icon.data, icon.data.length));
    }

    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StackIcon)) {
            return false;
        }
        StackIcon another = (StackIcon)obj;
        return Objects.equal(name, another.name) &&
               Objects.equal(mediaType, another.mediaType) &&
               Arrays.equals(data, another.data);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(mediaType);
        hash = 31 * hash + Arrays.hashCode(data);
        return hash;
    }

    @Override
    public String toString() {
        return "StackIcon{" +
               "name='" + name + '\'' +
               ", mediaType='" + mediaType + '\'' +
               ", data=[byte array]" +
               '}';
    }
}
