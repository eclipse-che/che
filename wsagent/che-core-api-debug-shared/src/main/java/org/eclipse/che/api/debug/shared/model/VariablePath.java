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
