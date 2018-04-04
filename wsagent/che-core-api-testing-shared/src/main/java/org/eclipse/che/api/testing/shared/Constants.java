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
package org.eclipse.che.api.testing.shared;

/** @author David Festal */
public class Constants {

  /** Name of WebSocket channel for the Testing output */
  @Deprecated public static final String TESTING_OUTPUT_CHANNEL_NAME = "testing:output";

  public static final String TESTING_RPC_METHOD_NAME = "testing/message";
  public static final String TESTING_RPC_TEST_DETECTION_NAME = "testing/testDetection";

  public static final String RUN_TESTS_METHOD = "testing/runTest";

  public static final String NAME = "name";
  public static final String ATTRIBUTES = "attributes";

  private Constants() {}
}
