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
package org.eclipse.che.api.debug.shared.model;

import java.util.List;

/**
 * Path to the variable. If it is primitive then it only consists of its name: ["varName"]. If it is
 * structured then it consists of all subsequent names: ["structure1Name", "structure2Name", ...,
 * "varName"]
 *
 * @author Anatoliy Bazko
 */
public interface VariablePath {
  List<String> getPath();
}
