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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.ConflictException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/** Class to hold image information such as data, name, media type */
public class FactoryImage {
    private byte[] imageData;
    private String mediaType;
    private String name;

    public FactoryImage() {
    }

    public FactoryImage(byte[] data, String mediaType, String name) throws IOException {
        setMediaType(mediaType);
        this.name = name;
        setImageData(data);
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) throws IOException {
        this.imageData = imageData;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) throws IOException {
        if (mediaType != null) {
            switch (mediaType) {
                case "image/jpeg":
                case "image/png":
                case "image/gif":
                    this.mediaType = mediaType;
                    return;
                default:
                    throw new IOException("Image media type '" + mediaType + "' is unsupported.");
            }
        }
        throw new IOException("Image media type 'null' is unsupported.");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasContent() {
        return imageData != null && imageData.length > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FactoryImage)) return false;

        FactoryImage that = (FactoryImage)o;

        if (!Arrays.equals(imageData, that.imageData)) return false;
        if (mediaType != null ? !mediaType.equals(that.mediaType) : that.mediaType != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = imageData != null ? Arrays.hashCode(imageData) : 0;
        result = 31 * result + (mediaType != null ? mediaType.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * Creates {@code FactoryImage}.
     * InputStream should be closed manually.
     *
     * @param is
     *         - input stream with image data
     * @param mediaType
     *         - media type of image
     * @param name
     *         - image name
     * @return - {@code FactoryImage} if {@code FactoryImage} was created, null if input stream has no content
     * @throws org.eclipse.che.api.core.ConflictException
     */
    public static FactoryImage createImage(InputStream is, String mediaType, String name) throws ConflictException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
                if (baos.size() > 1024 * 1024) {
                    throw new ConflictException("Maximum upload size exceeded.");
                }
            }

            if (baos.size() == 0) {
                return new FactoryImage();
            }
            baos.flush();

            return new FactoryImage(baos.toByteArray(), mediaType, name);
        } catch (IOException e) {
            throw new ConflictException(e.getLocalizedMessage());
        }
    }
}
