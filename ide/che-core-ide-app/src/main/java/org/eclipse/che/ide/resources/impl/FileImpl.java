/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.api.resources.marker.PresentableTextMarker;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.TextUtils;

/**
 * Default implementation of the {@code File}.
 *
 * @author Vlad Zhukovskyi
 * @see ResourceImpl
 * @see File
 * @since 4.4.0
 */
@Beta
class FileImpl extends ResourceImpl implements File {

  private final String contentUrl;
  private VcsStatus vcsStatus;

  private String modificationStamp;

  @Inject
  protected FileImpl(
      @Assisted Path path,
      @Assisted String contentUrl,
      @Assisted ResourceManager resourceManager,
      @Assisted VcsStatus vcsStatus) {
    super(path, resourceManager);

    this.contentUrl = contentUrl;
    this.vcsStatus = vcsStatus;
  }

  /** {@inheritDoc} */
  @Override
  public final int getResourceType() {
    return FILE;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public String getContentUrl() {
    return contentUrl;
  }

  /** {@inheritDoc} */
  @Override
  public String getDisplayName() {
    final Optional<Marker> optMarker = getMarker(PresentableTextMarker.ID);

    if (optMarker.isPresent()) {
      final PresentableTextMarker marker = (PresentableTextMarker) optMarker.get();

      return marker.getPresentableText();
    } else {
      return getName();
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getExtension() {
    final String entryName = getName();
    int lastDotIndex = entryName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return null;
    }

    if (lastDotIndex == (entryName.length() - 1)) {
      return "";
    }

    return entryName.substring(lastDotIndex + 1);
  }

  /** {@inheritDoc} */
  @Override
  public String getNameWithoutExtension() {
    final String entryName = getName();
    int lastDotIndex = entryName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return getName();
    }

    return entryName.substring(0, lastDotIndex);
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> updateContent(String content) {
    updateModificationStamp(content);

    return resourceManager.write(this, content);
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> getContent() {
    return resourceManager.read(this);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("path", getLocation())
        .add("resource", getResourceType())
        .add("contentUrl", contentUrl)
        .toString();
  }

  @Override
  public void setModificationStamp(String modificationStamp) {
    this.modificationStamp = modificationStamp;
  }

  @Override
  public String getModificationStamp() {
    return modificationStamp;
  }

  @Override
  public void updateModificationStamp(String content) {
    this.modificationStamp = TextUtils.md5(content);
  }

  @Override
  public VcsStatus getVcsStatus() {
    return vcsStatus;
  }

  @Override
  public void setVcsStatus(VcsStatus vcsStatus) {
    this.vcsStatus = vcsStatus;
  }

  @Override
  public Location toLocation(int lineNumber) {
    return new LocationImpl(getLocation().toString(), lineNumber, getProject().getPath());
  }
}
