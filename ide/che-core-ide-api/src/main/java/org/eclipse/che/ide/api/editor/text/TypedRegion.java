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
