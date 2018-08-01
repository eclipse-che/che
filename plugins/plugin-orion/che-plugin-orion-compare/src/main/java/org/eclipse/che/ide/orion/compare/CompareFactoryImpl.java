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
package org.eclipse.che.ide.orion.compare;

import com.google.inject.Singleton;
import org.eclipse.che.ide.orion.compare.jso.CompareConfigJs;
import org.eclipse.che.ide.orion.compare.jso.FileOptionsJs;

/**
 * Implementation for {@link CompareFactory}. This implementation creates JSO objects.
 *
 * @author Evgen Vidolob
 */
@Singleton
class CompareFactoryImpl implements CompareFactory {

  @Override
  public FileOptions createFieOptions() {
    return FileOptionsJs.createObject().<FileOptionsJs>cast();
  }

  @Override
  public CompareConfig createCompareConfig() {
    return CompareConfigJs.createObject().<CompareConfigJs>cast();
  }
}
