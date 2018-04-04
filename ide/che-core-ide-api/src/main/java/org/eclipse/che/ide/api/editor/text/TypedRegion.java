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
package org.eclipse.che.ide.api.editor.text;

/**
 * Describes a region of an indexed text store such as a document or a string. The region consists
 * of offset, length, and type. The region type is defined as a string.
 *
 * <p>A typed region can, e.g., be used to described document partitions.
 *
 * <p>Clients may implement this interface or use the standard implementation {@link
 * org.eclipse.TypedRegionImpl.text.TypedRegion}.
 */
public interface TypedRegion extends Region {

  /**
   * Returns the content type of the region.
   *
   * @return the content type of the region
   */
  String getType();
}
