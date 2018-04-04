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
package org.eclipse.che.api.core.model.project.type;

import java.util.List;

/**
 * Model interface for Project Type
 *
 * @author gazarenkov
 */
public interface ProjectType {

  /** @return unique ID */
  String getId();

  /** @return project type display name */
  String getDisplayName();

  /** @return true if this project type can be mixed in */
  boolean isMixable();

  /** @return true if this project type can be used as primary */
  boolean isPrimaryable();

  /**
   * @return true if this project type explicitly stored as is in the project description otherwise
   *     it is considered as "runtime" and can be calculated runtime using defined mandatory
   *     attributes thanks to Value Provider mechanism
   */
  boolean isPersisted();

  /** @return attributes */
  List<? extends Attribute> getAttributes();

  /** @return parent project type IDs */
  List<String> getParents();
}
