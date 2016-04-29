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

import java.util.Arrays;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Class for storing {@link org.eclipse.che.api.workspace.shared.stack.Stack} icon data
 *
 * @author Alexander Andrienko
 */
public class StackIcon {

    private static final Set<String> VALID_MEDIA_TYPES = of("image/jpeg", "image/png", "image/gif", "image/svg+xml");
    private static final int         LIMIT_SIZE        = 1024 * 1024;

    private String name;
    private String mediaType;
    private byte[] data;

    public StackIcon(String name, String mediaType, @Nullable byte[] data) {
        if (data != null) {
            if (data.length == 0) {
                throw new IllegalArgumentException("Incorrect icon data or icon was not attached");
            }
            if (data.length > LIMIT_SIZE) {
                throw new IllegalArgumentException("Maximum upload size exceeded 1 Mb limit");
            }
        }
        this.data = data;

        requireNonNull(mediaType, "Icon media type required");
        if (!VALID_MEDIA_TYPES.stream().anyMatch(elem -> elem.equals(mediaType))) {
            String errorMessage = format("Media type '%s' is unsupported. Supported media types: '%s'", mediaType, VALID_MEDIA_TYPES);
            throw new IllegalArgumentException(errorMessage);
        }
        this.mediaType = mediaType;

        this.name = requireNonNull(name, "Icon name required");
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
}
