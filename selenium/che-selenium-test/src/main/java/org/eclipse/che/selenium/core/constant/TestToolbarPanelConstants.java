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
package org.eclipse.che.selenium.core.constant;

/** @author Musienko Maxim */
public interface TestToolbarPanelConstants {
  interface Actions {
    String DELETE = "gwt-debug-ActionButton/deleteItem-true";
  }

  interface MachineDropDown {
    String COMMAND_DROPDAWN_XPATH = "//div[@id='gwt-debug-dropDownHeader'][position()='1']";
    String DEV_MACHINE = "gwt-debug-MachinesGroup/dev-machine";
    String DB_MACHINE = "gwt-debug-MachinesGroup/db";
  }
}
