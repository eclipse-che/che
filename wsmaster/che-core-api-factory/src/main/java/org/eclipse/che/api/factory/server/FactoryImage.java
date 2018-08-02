/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server;

import java.util.Arrays;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/** Class to hold image information such as data, name, media type */
@Embeddable
public class FactoryImage {

  @Column(name = "image_data")
  private byte[] imageData;

  @Column(name = "media_type")
  private String mediaType;

  @Column(name = "name")
  private String name;

  public FactoryImage() {}

  public FactoryImage(byte[] data, String mediaType, String name) {
    setMediaType(mediaType);
    this.name = name;
    setImageData(data);
  }

  public byte[] getImageData() {
    return imageData;
  }

  public void setImageData(byte[] imageData) {
    this.imageData = imageData;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    if (mediaType != null) {
      switch (mediaType) {
        case "image/jpeg":
        case "image/png":
        case "image/gif":
          this.mediaType = mediaType;
          return;
        default:
          throw new IllegalArgumentException(
              "Image media type '" + mediaType + "' is unsupported.");
      }
    }
    throw new IllegalArgumentException("Image media type 'null' is unsupported.");
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
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof FactoryImage)) return false;
    final FactoryImage other = (FactoryImage) obj;
    return Arrays.equals(imageData, other.imageData)
        && Objects.equals(mediaType, other.mediaType)
        && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Arrays.hashCode(imageData);
    hash = 31 * hash + Objects.hashCode(mediaType);
    hash = 31 * hash + Objects.hashCode(name);
    return hash;
  }
}
