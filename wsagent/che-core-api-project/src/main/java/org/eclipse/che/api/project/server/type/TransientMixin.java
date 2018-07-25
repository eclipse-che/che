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
package org.eclipse.che.api.project.server.type;

/**
 * Mixin-only type not for storing. I.e. accessory of project to this type is recognized on-fly.
 * Useful when we do not impact directly on some project aspect For instance we use it for VCS
 * project types as we do not assign and manage (change attributes) it directly.
 *
 * @author gazarenkov
 */
public abstract class TransientMixin extends ProjectTypeDef {

  /**
   * Mixable is always "true", Primaryable is always "false"
   *
   * @param id
   * @param displayName
   */
  protected TransientMixin(String id, String displayName) {
    super(id, displayName, false, true, false);
  }
}
