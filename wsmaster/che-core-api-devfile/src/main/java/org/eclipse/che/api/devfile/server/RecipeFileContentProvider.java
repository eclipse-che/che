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
package org.eclipse.che.api.devfile.server;

/**
 * Fetches content of file described in local field of recipe-type {@link
 * org.eclipse.che.api.devfile.model.Tool}
 *
 * @author Max Shaposhnyk
 */
@FunctionalInterface
public interface RecipeFileContentProvider {

  String fetchContent(String local);
}
