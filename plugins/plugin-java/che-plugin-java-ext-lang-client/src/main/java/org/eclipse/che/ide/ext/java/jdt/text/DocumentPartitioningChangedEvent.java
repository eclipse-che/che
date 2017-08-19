/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.jdt.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.text.RegionImpl;
import org.eclipse.che.ide.runtime.Assert;

/** Event describing the change of document partitionings. */
public class DocumentPartitioningChangedEvent {

  /** The document whose partitionings changed */
  private final Document fDocument;

  /** The map of partitionings to changed regions. */
  private final Map<String, Region> fMap = new HashMap<String, Region>();

  /**
   * Creates a new document partitioning changed event for the given document. Initially this event
   * is empty, i.e. does not describe any change.
   *
   * @param document the changed document
   */
  public DocumentPartitioningChangedEvent(Document document) {
    fDocument = document;
  }

  /**
   * Returns the changed document.
   *
   * @return the changed document
   */
  public Document getDocument() {
    return fDocument;
  }

  /**
   * Returns the changed region of the given partitioning or <code>null</code> if the given
   * partitioning did not change.
   *
   * @param partitioning the partitioning
   * @return the changed region of the given partitioning or <code>null</code>
   */
  public Region getChangedRegion(String partitioning) {
    return (Region) fMap.get(partitioning);
  }

  /**
   * Returns the set of changed partitionings.
   *
   * @return the set of changed partitionings
   */
  public String[] getChangedPartitionings() {
    String[] partitionings = new String[fMap.size()];
    fMap.keySet().toArray(partitionings);
    return partitionings;
  }

  /**
   * Sets the specified range as changed region for the given partitioning.
   *
   * @param partitioning the partitioning
   * @param offset the region offset
   * @param length the region length
   */
  public void setPartitionChange(String partitioning, int offset, int length) {
    Assert.isNotNull(partitioning);
    fMap.put(partitioning, new RegionImpl(offset, length));
  }

  /**
   * Returns <code>true</code> if the set of changed partitionings is empty, <code>false</code>
   * otherwise.
   *
   * @return <code>true</code> if the set of changed partitionings is empty
   */
  public boolean isEmpty() {
    return fMap.isEmpty();
  }

  /**
   * Returns the coverage of this event. This is the minimal region that contains all changed
   * regions of all changed partitionings.
   *
   * @return the coverage of this event
   */
  public Region getCoverage() {
    if (fMap.isEmpty()) return new RegionImpl(0, 0);

    int offset = -1;
    int endOffset = -1;
    Iterator<Region> e = fMap.values().iterator();
    while (e.hasNext()) {
      Region r = e.next();

      if (offset < 0 || r.getOffset() < offset) offset = r.getOffset();

      int end = r.getOffset() + r.getLength();
      if (end > endOffset) endOffset = end;
    }

    return new RegionImpl(offset, endOffset - offset);
  }
}
