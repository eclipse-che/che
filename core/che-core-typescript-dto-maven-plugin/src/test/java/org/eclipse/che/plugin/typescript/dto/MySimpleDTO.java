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
package org.eclipse.che.plugin.typescript.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author Florent Benoit */
@DTO
public interface MySimpleDTO {

  int getId();

  MySimpleDTO withId(int id);

  boolean getBoolean();

  MySimpleDTO withBoolean(boolean bool);

  double getDouble();

  MySimpleDTO withDouble(double d);

  float getFloat();

  MySimpleDTO withFloat(float f);
}
