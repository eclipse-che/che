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
package org.eclipse.che.api.git.shared;

/**
 * Branch list modes.
 *
 * @author Igor Vinokur
 */
public enum BranchListMode {
  /**
   * Show both remote and local branches. <br>
   * Corresponds to -a option in console git.
   */
  LIST_ALL,

  /**
   * Show only remote branches. <br>
   * Corresponds to -r option in console git.
   */
  LIST_REMOTE,

  /**
   * Show only local branches. <br>
   * Corresponds to -l or empty option in console git.
   */
  LIST_LOCAL;

  public static BranchListMode from(String mode) {
    switch (mode.toLowerCase()) {
      case "all":
        return LIST_ALL;
      case "remote":
        return LIST_REMOTE;
      case "local":
        return LIST_LOCAL;
      default:
        return null;
    }
  }
}
