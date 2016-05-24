/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.che;

import org.eclipse.che.plugin.java.server.CodeAssist;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Evgen Vidolob
 */
// TODO: rework after new Project API
@Ignore
public class CodeAssistantTest  extends BaseTest{
    @Test
    public void testFirst() throws Exception {
        StringBuilder b = new StringBuilder("package org.eclipse.che.test;\n");
        b.append("public class MyClass {\n");
        b.append("  public MyClass(int i){\n");
        b.append("   i\n");
        b.append("}\n}");
        int offset = b.indexOf("   i");
        CodeAssist codeAssist = new CodeAssist();
//        Proposals proposals = codeAssist.computeProposals(project, "org.eclipse.che.test.MyClass", offset, b.toString());
//        assertThat(proposals).isNotNull();
//        assertThat(proposals.getProposals()).isNotEmpty();
    }
}
