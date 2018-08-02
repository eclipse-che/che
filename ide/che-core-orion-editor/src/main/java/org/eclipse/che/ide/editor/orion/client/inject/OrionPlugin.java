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
package org.eclipse.che.ide.editor.orion.client.inject;

/**
 * Interface for providing information needed for registering Orion plugin.
 *
 * <p>Implementations of this interface need to be registered using a multibinder in order to be
 * picked-up by CHE.
 *
 * @author Artem Zatsarynnyi
 */
public interface OrionPlugin {

  /**
   * Returns path to the Orion plugin's html file. Path should be relative to the GWT's 'public'
   * folder.
   */
  String getRelPath();
}
