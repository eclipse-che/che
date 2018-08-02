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
package org.eclipse.che.api.languageserver.util;

import com.google.gwt.json.client.JSONValue;

/**
 * Extend JsonSerializable with a conversion to a json value. This is used in the generated DTO
 * conversions.
 *
 * @author Thomas Mäder
 */
public interface JsonSerializable extends org.eclipse.che.ide.dto.JsonSerializable {
  JSONValue toJsonElement();
}
