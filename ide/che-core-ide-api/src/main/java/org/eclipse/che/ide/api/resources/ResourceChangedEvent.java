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
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resource change events describe changes to resources.
 * <p/>
 * Each event has resource delta. Delta means some information, which was performed to a resource. To obtain information
 * about specific type of event, then method {@link ResourceDelta#getKind()} should be called.
 * <p/>
 * Third party components should implement {@link ResourceChangedHandler} to handle resource event and subscribe this
 * implementation in general event bus.
 * <p/>
 * Example of usage:
 * <pre>
 *     eventBus.addHandler(ResourceChangedEvent.getType(), new ResourceChangedHandler{
 *         void onResourceChanged(ResourceChangedEvent event) {
 *             ResourceDelta delta = event.getDelta();
 *             Resource resource = delta.getResource();
 *
 *             if (delta.getKind() == ResourceDelta.ADDED) {
 *                 //handle resource creation
 *             }
 *
 *             if (delta.getKind() == ResourceDelta.REMOVED) {
 *                 //handle resource removal
 *             }
 *
 *             if (delta.getKind() == ResourceDelta.UPDATED) {
 *                 //handle resource update
 *             }
 *         }
 *     });
 * </pre>
 *
 * @author Vlad Zhukovskiy
 * @see ResourceDelta
 * @since 4.4.0
 */
@Beta
public final class ResourceChangedEvent extends GwtEvent<ResourceChangedEvent.ResourceChangedHandler> {

    /**
     * A resource change listener is notified of changes to resources in the workspace. These changes arise
     * from direct manipulation of resources, or indirectly through re-synchronization with the local file
     * system.
     * <p/>
     * Third party components may implement this interface to handle resource changes event.
     *
     * @since 4.4.0
     */
    public interface ResourceChangedHandler extends EventHandler {

        /**
         * Notifies the listener that some resource changes are happening. The supplied event dives details.
         * This event object (and the resource delta within it) is valid only for the duration of the invocation
         * of this method.
         *
         * @param event
         *         instance of {@link ResourceChangedEvent}
         * @see ResourceChangedEvent
         * @since 4.4.0
         */
        void onResourceChanged(ResourceChangedEvent event);
    }

    private static Type<ResourceChangedHandler> TYPE;

    public static Type<ResourceChangedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private ResourceDelta delta;

    public ResourceChangedEvent(ResourceDelta delta) {
        this.delta = checkNotNull(delta, "Resource delta should not be a null");
    }

    /**
     * Returns the resource delta.
     *
     * @return the resource delta
     * @see ResourceDelta
     * @since 4.4.0
     */
    public ResourceDelta getDelta() {
        return delta;
    }

    /** {@inheritDoc} */
    @Override
    public Type<ResourceChangedHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(ResourceChangedHandler handler) {
        handler.onResourceChanged(this);
    }
}
