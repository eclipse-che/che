/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial commit
 *******************************************************************************/

package org.eclipse.che.api.testing.shared;

/**
 * @author David Festal
 */
public class Constants {

    /** Name of WebSocket channel for the Testing output */
    @Deprecated
    public final static String TESTING_OUTPUT_CHANNEL_NAME = "testing:output";

    public static final String TESTING_RPC_METHOD_NAME         = "testing/message";
    public static final String TESTING_RPC_TEST_DETECTION_NAME = "testing/testDetection";

    public static final String RUN_TESTS_METHOD = "testing/runTest";

    public static final String NAME       = "name";
    public static final String ATTRIBUTES = "attributes";

    private Constants() {
    }

}
