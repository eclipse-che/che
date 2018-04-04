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
