/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth1;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BitbucketServerOAuthAuthenticatorProviderTest {
  private File cfgFile;
  private File emptyFile;

  @BeforeClass
  public void setup() throws IOException {
    cfgFile = File.createTempFile("BitbucketServerOAuthAuthenticatorProviderTest-", "-cfg");
    Files.asCharSink(cfgFile, Charset.defaultCharset()).write("tmp-data");
    cfgFile.deleteOnExit();
    emptyFile = File.createTempFile("BitbucketServerOAuthAuthenticatorProviderTest-", "-empty");
    emptyFile.deleteOnExit();
  }

  @Test(dataProvider = "noopConfig")
  public void shouldProvideNoopOAuthAuthenticatorIfSomeConfigurationIsNotSet(
      String consumerKeyPath, String privateKeyPath, String bitbucketEndpoint) throws IOException {
    // given
    BitbucketServerOAuthAuthenticatorProvider provider =
        new BitbucketServerOAuthAuthenticatorProvider(
            consumerKeyPath, privateKeyPath, bitbucketEndpoint, "http://che.server.com");
    // when
    OAuthAuthenticator actual = provider.get();
    // then
    assertNotNull(actual);
    assertTrue(NoopOAuthAuthenticator.class.isAssignableFrom(actual.getClass()));
  }

  @Test
  public void shouldBeAbleToConfigureValidBitbucketServerOAuthAuthenticator() throws IOException {
    // given
    BitbucketServerOAuthAuthenticatorProvider provider =
        new BitbucketServerOAuthAuthenticatorProvider(
            cfgFile.getPath(), cfgFile.getPath(), "http://bitubucket.com", "http://che.server.com");
    // when
    OAuthAuthenticator actual = provider.get();
    // then
    assertNotNull(actual);
    assertTrue(BitbucketServerOAuthAuthenticator.class.isAssignableFrom(actual.getClass()));
  }

  @DataProvider(name = "noopConfig")
  public Object[][] noopConfig() {
    return new Object[][] {
      {null, null, null},
      {cfgFile.getPath(), null, null},
      {null, cfgFile.getPath(), null},
      {cfgFile.getPath(), cfgFile.getPath(), null},
      {emptyFile.getPath(), null, null},
      {null, emptyFile.getPath(), null},
      {emptyFile.getPath(), emptyFile.getPath(), null},
      {cfgFile.getPath(), emptyFile.getPath(), null},
      {emptyFile.getPath(), cfgFile.getPath(), null},
      {emptyFile.getPath(), emptyFile.getPath(), "http://bitubucket.com"},
      {cfgFile.getPath(), emptyFile.getPath(), "http://bitubucket.com"},
      {emptyFile.getPath(), cfgFile.getPath(), "http://bitubucket.com"},
      {null, null, "http://bitubucket.com"}
    };
  }
}
