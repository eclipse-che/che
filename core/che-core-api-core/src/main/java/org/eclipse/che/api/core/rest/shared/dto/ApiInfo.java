/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface ApiInfo {
  String getSpecificationVendor();

  ApiInfo withSpecificationVendor(String specificationVendor);

  void setSpecificationVendor(String specificationVendor);

  String getImplementationVendor();

  ApiInfo withImplementationVendor(String implementationVendor);

  void setImplementationVendor(String implementationVendor);

  String getSpecificationTitle();

  ApiInfo withSpecificationTitle(String specificationTitle);

  void setSpecificationTitle(String specificationTitle);

  String getSpecificationVersion();

  ApiInfo withSpecificationVersion(String specificationVersion);

  void setSpecificationVersion(String specificationVersion);

  String getImplementationVersion();

  ApiInfo withImplementationVersion(String implementationVersion);

  void setImplementationVersion(String implementationVersion);

  String getScmRevision();

  ApiInfo withScmRevision(String scmRevision);

  void setScmRevision(String scmRevision);

  String getIdeVersion();

  ApiInfo withIdeVersion(String ideVersion);

  void setIdeVersion(String ideVersion);

  String getBuildInfo();

  ApiInfo withBuildInfo(String buildInfo);

  void setBuildInfo(String buildInfo);
}
