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
package org.eclipse.che.ide.api.parts;

/**
 * Interface for listening for property changes on an {@link PartPresenter}
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 */
public interface PropertyListener {

  /**
   * Indicates that a property has changed.
   *
   * @param source the object whose property has changed
   * @param propId the id of the property which has changed; property ids are generally defined as
   *     constants on the source class
   */
  void propertyChanged(PartPresenter source, int propId);
}
