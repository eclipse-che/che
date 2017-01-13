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
package org.eclipse.che.ide.api.resources.marker;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.resources.Resource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Marker created event. This event fires when marker has been created.
 * Created, means that marker has bound to the resource.
 *
 * @author Vlad Zhukovskiy
 * @see Marker
 * @since 4.4.0
 */
@Beta
public class MarkerChangedEvent extends GwtEvent<MarkerChangedEvent.MarkerChangedHandler> {

    /**
     * A marker create listener is notified of marker creation to the specified resource.
     * <p/>
     * Third party components may implement this interface to handle marker creation event.
     */
    public interface MarkerChangedHandler extends EventHandler {

        /**
         * Notifies the listener that some marker has been created.
         *
         * @param event
         *         instance of {@link MarkerChangedEvent}
         * @see MarkerChangedEvent
         * @since 4.4.0
         */
        void onMarkerChanged(MarkerChangedEvent event);
    }

    private static Type<MarkerChangedHandler> TYPE;

    public static Type<MarkerChangedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final Resource resource;
    private final Marker   marker;
    private final int      status;

    public MarkerChangedEvent(Resource resource, Marker marker, int status) {
        this.status = status;
        this.resource = checkNotNull(resource, "Resource should not be a null");
        this.marker = checkNotNull(marker, "Marker should not be a null");
    }

    /**
     * Returns the resource which is the host of specified marker.
     *
     * @return the resource
     * @since 4.4.0
     */
    public final Resource getResource() {
        return resource;
    }

    /**
     * Returns the marker which is bounded to resource provided by {@link #getResource()}.
     *
     * @return the marker
     * @see Marker
     * @since 4.4.0
     */
    public Marker getMarker() {
        return marker;
    }

    /**
     * Returns the status of event.
     *
     * @return the status
     * @see Marker#CREATED
     * @see Marker#REMOVED
     * @see Marker#UPDATED
     * @since 4.4.0
     */
    public int getStatus() {
        return status;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("resource", resource)
                          .add("marker", marker)
                          .add("status", status)
                          .toString();
    }

    /** {@inheritDoc} */
    @Override
    public Type<MarkerChangedHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(MarkerChangedHandler handler) {
        handler.onMarkerChanged(this);
    }
}
