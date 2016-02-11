/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.shared;

/** @author Vladyslav Zhukovskii */
public interface AntAttributes {
    final String ANT_ID   = "ant";
    final String ANT_NAME = "Ant Project";

    final String ANT_GENERATOR_ID = "ant";

    final String SOURCE_FOLDER      = "ant.source.folder";
    final String TEST_SOURCE_FOLDER = "ant.test.source.folder";

    final String DEF_SRC_PATH      = "src";
    final String DEF_TEST_SRC_PATH = "test";

    final String BUILD_FILE = "build.xml";
}
