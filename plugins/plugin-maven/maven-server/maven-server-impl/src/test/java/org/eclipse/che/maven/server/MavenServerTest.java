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

import org.eclipse.che.maven.data.MavenActivation;
import org.eclipse.che.maven.data.MavenActivationFile;
import org.eclipse.che.maven.data.MavenExplicitProfiles;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenProfile;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Properties;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

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
                .getEffectivePom(new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()), emptyList(),
                                 emptyList());
        Assert.assertNotNull(effectivePom);
    }

    @Test
    public void testCustomComponents() throws Exception {
        MavenSettings mavenSettings = new MavenSettings();
        mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_DEBUG);
        MavenServerImpl mavenServerImpl = new MavenServerImpl(mavenSettings);
        boolean[] isPrintCalled = new boolean[]{false};
        mavenServerImpl.setComponents(null, false, new MavenTerminal() {

            @Override
            public void print(int level, String message, Throwable throwable) throws RemoteException {
                isPrintCalled[0] = true;
            }
        }, null, false);
        mavenServerImpl
                .getEffectivePom(new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()), emptyList(),
                                 emptyList());
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

    @Test
    public void profilesShouldBeAnalyzed() throws Exception {
        MavenModel model = new MavenModel();
        model.setMavenKey(new MavenKey("aaa", "bbb", "ccc"));
        model.getBuild().setSources(Collections.singletonList("src/main/java"));
        model.getBuild().setTestSources(Collections.singletonList("src/test/java"));

        MavenProfile profile = new MavenProfile("id", "pom");
        final MavenActivation activation = new MavenActivation();
        activation.setActiveByDefault(true);
        profile.setActivation(activation);
        final Properties properties = new Properties();
        properties.setProperty("key", "value");
        profile.setProperties(properties);
        model.setProfiles(singletonList(profile));


        final ProfileApplicationResult profileApplicationResult = MavenServerImpl
                .applyProfiles(model,
                               new File(MavenServerTest.class.getResource("/multi-module-with-profiles/pom.xml").getFile()),
                               MavenExplicitProfiles.NONE, emptyList());
        Assert.assertNotNull(profileApplicationResult);
        Assert.assertEquals(1, profileApplicationResult.getActivatedProfiles().getEnabledProfiles().size());
        Assert.assertEquals(1, profileApplicationResult.getModel().getProperties().size());
    }

    @Test
    public void profilesShouldBeActivatedBeExistingFile() throws Exception {
        MavenModel model = new MavenModel();
        model.setMavenKey(new MavenKey("aaa", "bbb", "ccc"));
        model.getBuild().setSources(Collections.singletonList("src/main/java"));
        model.getBuild().setTestSources(Collections.singletonList("src/test/java"));

        MavenProfile profile = new MavenProfile("id", "pom");
        final MavenActivation activation = new MavenActivation();
        activation.setFile(new MavenActivationFile("${basedir}/dir/file.txt", ""));
        profile.setActivation(activation);
        final Properties properties = new Properties();
        properties.setProperty("key", "value");
        profile.setProperties(properties);
        model.setProfiles(singletonList(profile));


        final ProfileApplicationResult profileApplicationResult = MavenServerImpl
                .applyProfiles(model,
                               new File(MavenServerTest.class.getResource("/multi-module-with-profiles").getFile()),
                               MavenExplicitProfiles.NONE, emptyList());
        Assert.assertNotNull(profileApplicationResult);
        Assert.assertEquals(1, profileApplicationResult.getActivatedProfiles().getEnabledProfiles().size());
        Assert.assertEquals(1, profileApplicationResult.getModel().getProperties().size());
    }
}
