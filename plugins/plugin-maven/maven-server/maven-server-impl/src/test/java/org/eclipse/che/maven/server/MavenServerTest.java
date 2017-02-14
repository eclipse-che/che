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
package org.eclipse.che.maven.server;

import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collections;

/**
 * @author Evgen Vidolob
 */
public class MavenServerTest {

    @Test
    public void testServerCreation() throws RemoteException {
        MavenSettings mavenSettings = new MavenSettings();
        mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);
        mavenSettings.setGlobalSettings(new File(System.getProperty("user.home"), ".m2/settings.xml"));
        new MavenServerImpl(mavenSettings);
    }

    @Test
    public void testGetEffectivePom() throws Exception {
        MavenSettings mavenSettings = new MavenSettings();
        mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);
        String effectivePom = new MavenServerImpl(mavenSettings)
                .getEffectivePom(new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()), Collections.emptyList(),
                                 Collections.emptyList());
        Assert.assertNotNull(effectivePom);
    }

    @Test
    public void testCustomComponents() throws Exception {
        MavenSettings mavenSettings = new MavenSettings();
        mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_DEBUG);
        MavenServerImpl mavenServerImpl = new MavenServerImpl(mavenSettings);
        boolean[] isPrintCalled = new boolean[] {false};
        mavenServerImpl.setComponents(null, false, new MavenTerminal() {

            @Override
            public void print(int level, String message, Throwable throwable) throws RemoteException {
                isPrintCalled[0] = true;
            }
        }, null, false);
        mavenServerImpl
                .getEffectivePom(new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()), Collections.emptyList(),
                                 Collections.emptyList());
        Assert.assertTrue(isPrintCalled[0]);
    }

    @Test
    public void testInterpolateModel() throws Exception {
        MavenModel model = new MavenModel();
        model.setMavenKey(new MavenKey("aaa", "bbb", "ccc"));
        model.getBuild().setSources(Collections.singletonList("src/main/java"));
        model.getBuild().setTestSources(Collections.singletonList("src/test/java"));

        MavenModel interpolateModel =
                MavenServerImpl.interpolateModel(model, new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()));
        Assert.assertNotNull(interpolateModel);

    }
}
