package org.eclipse.che.api.core.jdbc.jpa.eclipselink;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventListener;

import java.util.Vector;

/**
 * Adapter class for receiving EclipseLink events.
 * The methods are empty, this class allows to implement
 * only needed methods.
 *
 * @author Yevhenii Voevodin
 */
public abstract class DescriptorEventAdapter implements DescriptorEventListener {

    @Override
    public void aboutToDelete(DescriptorEvent event) {}

    @Override
    public void aboutToInsert(DescriptorEvent event) {}

    @Override
    public void aboutToUpdate(DescriptorEvent event) {}

    @Override
    public boolean isOverriddenEvent(DescriptorEvent event, Vector eventManagers) { return false; }

    @Override
    public void postBuild(DescriptorEvent event) {}

    @Override
    public void postClone(DescriptorEvent event) {}

    @Override
    public void postDelete(DescriptorEvent event) {}

    @Override
    public void postInsert(DescriptorEvent event) {}

    @Override
    public void postMerge(DescriptorEvent event) {}

    @Override
    public void postRefresh(DescriptorEvent event) {}

    @Override
    public void postUpdate(DescriptorEvent event) {}

    @Override
    public void postWrite(DescriptorEvent event) {}

    @Override
    public void preDelete(DescriptorEvent event) {}

    @Override
    public void preInsert(DescriptorEvent event) {}

    @Override
    public void prePersist(DescriptorEvent event) {}

    @Override
    public void preRemove(DescriptorEvent event) {}

    @Override
    public void preUpdate(DescriptorEvent event) {}

    @Override
    public void preUpdateWithChanges(DescriptorEvent event) {}

    @Override
    public void preWrite(DescriptorEvent event) {}
}
