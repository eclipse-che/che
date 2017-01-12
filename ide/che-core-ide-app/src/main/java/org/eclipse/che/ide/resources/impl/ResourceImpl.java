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
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static org.eclipse.che.ide.api.resources.marker.Marker.CREATED;
import static org.eclipse.che.ide.api.resources.marker.Marker.REMOVED;
import static org.eclipse.che.ide.api.resources.marker.Marker.UPDATED;

/**
 * Default implementation of the {@code Resource}.
 *
 * @author Vlad Zhukovskyi
 * @see Resource
 * @since 4.4.0
 */
@Beta
abstract class ResourceImpl implements Resource {

    protected final ResourceManager resourceManager;
    protected final Path            path;

    protected Marker[] markers = new Marker[0];

    protected ResourceImpl(Path path, ResourceManager resourceManager) {
        this.path = checkNotNull(path.removeTrailingSeparator(), "Null path occurred");
        this.resourceManager = checkNotNull(resourceManager, "Null project manager occurred");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFile() {
        return getResourceType() == FILE;
    }

    @Override
    public File asFile() {
        checkState(isFile(), "Current resource is not a file");

        return (File)this;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFolder() {
        return getResourceType() == FOLDER;
    }

    @Override
    public Folder asFolder() {
        checkState(isFolder(), "Current resource is not a folder");

        return (Folder)this;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProject() {
        return getResourceType() == PROJECT;
    }

    @Override
    public Project asProject() {
        checkState(isProject(), "Current resource is not a project");

        return (Project)this;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource> copy(Path destination) {
        return copy(destination, false);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource> copy(Path destination, boolean force) {
        return resourceManager.copy(this, destination, force);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource> move(Path destination) {
        return move(destination, false);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource> move(Path destination, boolean force) {
        return resourceManager.move(this, destination, force);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> delete() {
        return resourceManager.delete(this);
    }

    /** {@inheritDoc} */
    @Override
    public Path getLocation() {
        return path;
    }

    /** {@inheritDoc} */
    @Override
    public Container getParent() {
        final Optional<Container> parent = resourceManager.parentOf(this);

        return parent.isPresent() ? parent.get() : null;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Project> getRelatedProject() {
        if (this instanceof Project) {
            return of((Project)this);
        }

        Container optionalParent = getParent();

        if (optionalParent == null) {
            return absent();
        }

        Container parent = optionalParent;

        while (!(parent instanceof Project)) {
            optionalParent = parent.getParent();

            if (optionalParent == null) {
                return absent();
            }

            parent = optionalParent;
        }

        return of((Project)parent);
    }

    @Override
    public Project getProject() {
        final Optional<Project> project = getRelatedProject();

        return project.isPresent() ? project.get() : null;
    }

    /** {@inheritDoc} */
    @Override
    public abstract int getResourceType();

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return path.isRoot() ? "Workspace" : path.lastSegment();
    }

    /** {@inheritDoc} */
    @Override
    public String getURL() {
        return resourceManager.getUrl(this);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Marker> getMarker(String type) {
        checkArgument(!isNullOrEmpty(type), "Invalid marker type occurred");

        if (markers.length == 0) {
            return absent();
        }

        for (Marker marker : markers) {
            if (marker.getType().equals(type)) {
                return of(marker);
            }
        }

        return absent();
    }

    /** {@inheritDoc} */
    @Override
    public Marker[] getMarkers() {
        return markers;
    }

    /** {@inheritDoc} */
    @Override
    public void addMarker(Marker marker) {
        checkArgument(marker != null, "Null marker occurred");

        for (int i = 0; i < markers.length; i++) {
            if (markers[i].getType().equals(marker.getType())) {
                markers[i] = marker;
                resourceManager.notifyMarkerChanged(this, marker, UPDATED);
                return;
            }
        }

        final int index = markers.length;
        markers = copyOf(markers, index + 1);
        markers[index] = marker;
        resourceManager.notifyMarkerChanged(this, marker, CREATED);
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteMarker(String type) {
        checkArgument(!isNullOrEmpty(type), "Invalid marker type occurred");

        int size = markers.length;
        int index = -1;

        for (int i = 0; i < markers.length; i++) {
            if (markers[i].getType().equals(type)) {
                index = i;
                resourceManager.notifyMarkerChanged(this, markers[i], REMOVED);
                break;
            }
        }

        if (index == -1) {
            return false;
        }

        final int numMoved = markers.length - index - 1;
        if (numMoved > 0) {
            arraycopy(markers, index + 1, markers, index, numMoved);
        }
        markers = copyOf(markers, --size);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteAllMarkers() {
        if (Arrays.isNullOrEmpty(markers)) {
            return false;
        }

        markers = new Marker[0];

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Resource> getParentWithMarker(String type) {
        checkArgument(!isNullOrEmpty(type), "Invalid marker type occurred");

        if (getMarker(type).isPresent()) {
            return Optional.<Resource>of(this);
        }

        Container optParent = getParent();

        while (optParent != null) {
            Container parent = optParent;

            final Optional<Marker> marker = parent.getMarker(type);
            if (marker.isPresent()) {
                return Optional.<Resource>of(parent);
            }

            optParent = parent.getParent();
        }

        return absent();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Resource)) {
            return false;
        }

        Resource resource = (Resource)o;
        return getResourceType() == resource.getResourceType() && getLocation().equals(resource.getLocation());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(getResourceType(), getLocation().toString());
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Resource o) {
        return getLocation().toString().compareTo(o.getLocation().toString());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("path", path)
                          .add("resource", getResourceType())
                          .toString();
    }
}
