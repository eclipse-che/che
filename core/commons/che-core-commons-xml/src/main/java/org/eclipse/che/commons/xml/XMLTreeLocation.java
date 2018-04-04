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
package org.eclipse.che.commons.xml;

import java.util.LinkedList;

/**
 * Helps to add new tree elements in specified places
 *
 * @author Eugene Voevodin
 */
public class XMLTreeLocation {

  /** Location which indicates position after element with given name */
  public static XMLTreeLocation after(String name) {
    return new XMLTreeLocation(LocationType.AFTER, name);
  }

  /** Location which indicates position before element with given name */
  public static XMLTreeLocation before(String name) {
    return new XMLTreeLocation(LocationType.BEFORE, name);
  }

  /** Location which indicates after last element position */
  public static XMLTreeLocation inTheEnd() {
    return new XMLTreeLocation(LocationType.END, "");
  }

  /** Location which indicates before first element position */
  public static XMLTreeLocation inTheBegin() {
    return new XMLTreeLocation(LocationType.BEGIN, "");
  }

  /** Indicates position after any of elements with given names */
  public static XMLTreeLocation afterAnyOf(String... names) {
    if (names.length == 0) {
      throw new IllegalArgumentException("Required not empty elements names");
    }
    return disjunctionChain(LocationType.AFTER, names);
  }

  /** Indicates position before any of elements with given names */
  public static XMLTreeLocation beforeAnyOf(String... names) {
    if (names.length == 0) {
      throw new IllegalArgumentException("Required not empty elements names");
    }
    return disjunctionChain(LocationType.BEFORE, names);
  }

  /** Connects locations with same type by {@link #or} connector */
  private static XMLTreeLocation disjunctionChain(LocationType location, String[] names) {
    final XMLTreeLocation treeLocation = new XMLTreeLocation(location, names[0]);
    for (int i = 1; i < names.length; i++) {
      treeLocation.or(new XMLTreeLocation(location, names[i]));
    }
    return treeLocation;
  }

  private LinkedList<XMLTreeLocation> locations;
  private LocationType location;
  private String name;

  private XMLTreeLocation(LocationType location, String name) {
    this.location = location;
    this.name = name;
  }

  public XMLTreeLocation or(XMLTreeLocation location) {
    locations().add(location);
    return this;
  }

  void evalInsert(Element parent, NewElement newElement) {
    locations().addFirst(this);
    for (XMLTreeLocation location : locations) {
      switch (location.location) {
        case AFTER:
          if (parent.hasSingleChild(location.name)) {
            parent.getSingleChild(location.name).insertAfter(newElement);
            return;
          }
          break;
        case BEFORE:
          if (parent.hasSingleChild(location.name)) {
            parent.getSingleChild(location.name).insertBefore(newElement);
            return;
          }
          break;
        case BEGIN:
          final Element first = parent.getFirstChild();
          if (first != null) {
            first.insertBefore(newElement);
          } else {
            parent.appendChild(newElement);
          }
          return;
        case END:
          parent.appendChild(newElement);
          return;
      }
    }
    throw new XMLTreeException("It is not possible to insert element in specified location");
  }

  private LinkedList<XMLTreeLocation> locations() {
    return locations == null ? locations = new LinkedList<>() : locations;
  }

  private enum LocationType {
    AFTER,
    BEFORE,
    BEGIN,
    END
  }
}
