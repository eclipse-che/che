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
