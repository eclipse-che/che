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
package org.eclipse.che.ide.api.action;

/**
 * A "PropertyChange" event gets delivered whenever a bean changes a "bound" or "constrained"
 * property. A PropertyChangeEvent object is sent as an argument to the PropertyChangeListener and
 * VetoableChangeListener methods.
 *
 * <p>Normally PropertyChangeEvents are accompanied by the name and the old and new value of the
 * changed property. If the new value is a primitive type (such as int or boolean) it must be
 * wrapped as the corresponding java.lang.* Object type (such as Integer or Boolean).
 *
 * <p>Null values may be provided for the old and the new values if their true values are not known.
 *
 * <p>An event source may send a null object as the name to indicate that an arbitrary set of if its
 * properties have changed. In this case the old and new values should also be null.
 *
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class PropertyChangeEvent {

  /** The object on which the Event initially occurred. */
  protected Object source;

  /**
   * name of the property that changed. May be null, if not known.
   *
   * @serial
   */
  private String propertyName;

  /**
   * New value for property. May be null if not known.
   *
   * @serial
   */
  private Object newValue;

  /**
   * Previous value for property. May be null if not known.
   *
   * @serial
   */
  private Object oldValue;

  /**
   * Constructs a new <code>PropertyChangeEvent</code>.
   *
   * @param source The bean that fired the event.
   * @param propertyName The programmatic name of the property that was changed.
   * @param oldValue The old value of the property.
   * @param newValue The new value of the property.
   */
  public PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
    this.source = source;
    this.propertyName = propertyName;
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  /**
   * Gets the programmatic name of the property that was changed.
   *
   * @return The programmatic name of the property that was changed. May be null if multiple
   *     properties have changed.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Gets the new value for the property, expressed as an Object.
   *
   * @return The new value for the property, expressed as an Object. May be null if multiple
   *     properties have changed.
   */
  public Object getNewValue() {
    return newValue;
  }

  /**
   * Gets the old value for the property, expressed as an Object.
   *
   * @return The old value for the property, expressed as an Object. May be null if multiple
   *     properties have changed.
   */
  public Object getOldValue() {
    return oldValue;
  }

  /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("[propertyName=").append(getPropertyName());
    sb.append("; oldValue=").append(getOldValue());
    sb.append("; newValue=").append(getNewValue());
    sb.append("; source=").append(getSource());
    return sb.append("]").toString();
  }

  /**
   * The object on which the Event initially occurred.
   *
   * @return The object on which the Event initially occurred.
   */
  public Object getSource() {
    return source;
  }
}
