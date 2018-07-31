/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.resources.reveal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;

/**
 * Broadcast event to reveal given resource in active focused part.
 *
 * <p>Usually, part which handles this event should check if it is focused at this moment and if so,
 * process revealing the resource.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
public class RevealResourceEvent extends GwtEvent<RevealResourceEvent.RevealResourceHandler> {

  /**
   * A listener which notifies third-party components to reveal the specific resource.
   *
   * <p>Third-party components should check if their's part is focused at this moment and id so,
   * then process revealing the resource.
   */
  public interface RevealResourceHandler extends EventHandler {

    /**
     * Notifies the listener that given resource should be revealed by handled part.
     *
     * @param event instance of {@link RevealResourceEvent}
     * @see RevealResourceEvent
     * @since 4.4.0
     */
    void onRevealResource(RevealResourceEvent event);
  }

  private static Type<RevealResourceHandler> TYPE;

  public static Type<RevealResourceHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Path location;
  private boolean isSelectionRequired = true;
  private boolean isFocusRequired = true;

  public RevealResourceEvent(Resource resource) {
    this.location = checkNotNull(resource, "Resource should not be a null").getLocation();
  }

  public RevealResourceEvent(Path location) {
    this.location = checkNotNull(location, "Path should not be a null");
  }

  /**
   * Creates new event to reveal given resource.
   *
   * @param location the resource path which should be revealed
   * @param isSelectionRequired whether corresponding node should be selected after reveal
   * @param isFocusRequired whether corresponding node should be focused after reveal
   */
  public RevealResourceEvent(
      Path location, boolean isSelectionRequired, final boolean isFocusRequired) {
    this.location = checkNotNull(location, "Path should not be a null");
    this.isSelectionRequired = isSelectionRequired;
    this.isFocusRequired = isFocusRequired;
  }

  /**
   * Returns the resource path which should be revealed.
   *
   * @return the resource path
   * @since 4.4.0
   */
  public Path getLocation() {
    return location;
  }

  /**
   * Returns whether corresponding node should be selected after reveal
   *
   * @return {@code true} if corresponding node should be selected after reveal or {@code false}
   *     otherwise
   */
  public boolean isSelectionRequired() {
    return isSelectionRequired;
  }

  /**
   * Returns whether corresponding node should be focused after reveal
   *
   * @return {@code true} if corresponding node should be focused after reveal or {@code false}
   *     otherwise
   */
  public boolean isFocusRequired() {
    return isFocusRequired;
  }

  /** {@inheritDoc} */
  @Override
  public Type<RevealResourceHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(RevealResourceHandler handler) {
    handler.onRevealResource(this);
  }
}
