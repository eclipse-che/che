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
package org.eclipse.che.inject;

import com.google.inject.Module;
import java.util.List;

/**
 * Interface used for ServiceLoader mechanism. Implementations will have to implement this interface
 * to provide list of modules at runtime
 *
 * @author Florent Benoit
 */
public interface ModuleFinder {

  /**
   * Provides the list of additional modules
   *
   * @return the list of modules to add.
   */
  List<Module> getModules();
}
