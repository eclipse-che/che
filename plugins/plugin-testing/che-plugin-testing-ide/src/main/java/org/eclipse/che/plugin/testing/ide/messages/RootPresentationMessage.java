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

/**
 * Data class represents test root message.
 */
public class RootPresentationMessage extends ClientTestingMessage {

    RootPresentationMessage() {
    }

    @Override
    public void visit(TestingMessageVisitor visitor) {
        visitor.visitRootPresentation(this);
    }

    public String getRootName() {
        return getAttributeValue("name");
    }

    public String getComment() {
        return getAttributeValue("comment");
    }

    public String getLocation() {
        return getAttributeValue("location");
    }
}
