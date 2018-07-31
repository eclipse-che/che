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
