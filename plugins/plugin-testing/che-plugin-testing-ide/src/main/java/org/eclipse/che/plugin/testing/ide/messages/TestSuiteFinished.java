/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide.messages;

import org.eclipse.che.api.testing.shared.messages.TestingMessageNames;

/**
 *
 */
public class TestSuiteFinished extends BaseTestSuiteMessage {

    static {
        messageConstructors.put(TestingMessageNames.TEST_SUITE_FINISHED, TestSuiteFinished::new);
    }

    TestSuiteFinished() {
    }

    @Override
    public void visit(TestingMessageVisitor visitor) {
        visitor.visitTestSuiteFinished(this);
    }
}
