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
package org.eclipse.che.ide.statepersistance;

/**
 * Constants for the mappings to store/restore app state.
 *
 * @author Roman Nikitenko
 */
public final class AppStateConstants {

  public static final String APP_STATE = "IdeAppStates";
  public static final String WORKSPACE = "workspace";
  public static final String PERSPECTIVES = "perspectives";
  public static final String PART_STACKS = "PART_STACKS";
  public static final String PART_STACK_STATE = "STATE";
  public static final String PART_STACK_PARTS = "PARTS";
  public static final String PART_STACK_SIZE = "SIZE";
  public static final String ACTIVE_PART = "ACTIVE_PART";
  public static final String PART_CLASS_NAME = "CLASS";
  public static final String HIDDEN_STATE = "HIDDEN";

  private AppStateConstants() {}
}
