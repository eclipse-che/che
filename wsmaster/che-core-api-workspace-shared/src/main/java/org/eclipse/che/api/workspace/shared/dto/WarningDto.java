/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.dto.shared.DTO;

/** @author Yevhenii Voevodin */
@DTO
public interface WarningDto extends Warning {

  void setCode(int code);

  WarningDto withCode(int code);

  void setMessage(String message);

  WarningDto withMessage(String message);
}
