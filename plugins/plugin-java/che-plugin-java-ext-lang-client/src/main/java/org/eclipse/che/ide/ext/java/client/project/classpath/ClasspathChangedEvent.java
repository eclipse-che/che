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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;

/**
 * This event should be fired when classpath is changed.
 *
 * @author Valeriy Svydenko
 */
public class ClasspathChangedEvent extends GwtEvent<ClasspathChangedEvent.ClasspathChangedHandler> {

  /** Type class used to register this event. */
  public static Type<ClasspathChangedHandler> TYPE = new Type<>();

  private final List<ClasspathEntryDto> entries;
  private final String projectPath;

  /**
   * Creates an event to initiate changing of classpath.
   *
   * @param projectPath path to the project
   * @param entries classpath entries
   */
  public ClasspathChangedEvent(String projectPath, List<ClasspathEntryDto> entries) {
    this.projectPath = projectPath;
    this.entries = entries;
  }

  @Override
  public Type<ClasspathChangedHandler> getAssociatedType() {
    return TYPE;
  }

  /** Returns a path of the project. */
  public String getPath() {
    return projectPath;
  }

  /** Returns classpath entries. */
  public List<ClasspathEntryDto> getEntries() {
    return entries;
  }

  @Override
  protected void dispatch(ClasspathChangedHandler handler) {
    handler.onClasspathChanged(this);
  }

  /** Special handler which is called when classpath is changed */
  public interface ClasspathChangedHandler extends EventHandler {

    /**
     * Performs some actions when classpath is changed.
     *
     * @param event contains information about project which was selected
     */
    void onClasspathChanged(ClasspathChangedEvent event);
  }
}
