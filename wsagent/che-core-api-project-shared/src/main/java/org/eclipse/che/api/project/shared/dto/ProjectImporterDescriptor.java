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
package org.eclipse.che.api.project.shared.dto;

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/** @author Vitaly Parfonov */
@DTO
public interface ProjectImporterDescriptor {

  String getId();

  void setId(String id);

  ProjectImporterDescriptor withId(String id);

  /** @return importer's category (example: source control, archive) */
  String getCategory();

  /** @param category importer's category (example: source control, archive) */
  void setCategory(String category);

  ProjectImporterDescriptor withCategory(String category);

  /**
   * @return true if this importer uses only internal und not accessible for users call otherwise
   *     false
   */
  boolean isInternal();

  /**
   * set true if this importer uses only internal und not accessible for users call otherwise false
   */
  void setInternal(boolean isInternal);

  ProjectImporterDescriptor withInternal(boolean isInternal);

  /** Get description of project importer. */
  String getDescription();

  /** Set description of project importer. */
  void setDescription(String description);

  ProjectImporterDescriptor withDescription(String description);

  /** Gets attributes of this project importer. */
  Map<String, String> getAttributes();

  /** Sets attributes for this project importer. */
  void setAttributes(Map<String, String> attributes);

  ProjectImporterDescriptor withAttributes(Map<String, String> attributes);
}
