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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;

/** Factory for {@link JavaAnnotationModel} instances. */
public interface JavaAnnotationModelFactory {
  /**
   * Builds an instance of {@link JavaAnnotationModel}.
   *
   * @param docPositionMap a doc position map model
   * @return a java annotation model
   */
  JavaAnnotationModel create(DocumentPositionMap docPositionMap);
}
