/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.che.ide.util.browser;

import com.google.gwt.core.client.GWT;

/**
 * Class to allow conditional compilation for different user agents.
 *
 * <p>All methods should return values that are known at compile time. * TODO(user): Should this
 * ever be thrown in for free with GWT, rather use their approach. A relevant thread is:
 * http://groups.google.com/group/Google-Web-Toolkit-Contributors/browse_thread/thread/6745dee7a85eb585/bd58d1a9f2344b34
 */
public abstract class UserAgentStaticProperties {

  static UserAgentStaticProperties get() {
    return INSTANCE;
  }

  private static final UserAgentStaticProperties INSTANCE = createInstance();

  /**
   * Creates an instance of UserAgent.
   *
   * <p>NOTE(danilatos): This method is designed to be statically evaluable by the compiler, such
   * that the compiler can determine that only one subclass of UserAgent is ever used within a given
   * permutation. This is possible because GWT.isClient() is replaced with true by the compiler,
   * even though it is executed normally in unit tests. Testing the return value of GWT.create() is
   * not adequate because only boolean values can be statically evaluated by the compiler at this
   * time.
   *
   * @return an instance of UserAgent.
   */
  private static UserAgentStaticProperties createInstance() {
    if (GWT.isClient()) {
      return GWT.create(UserAgentStaticProperties.class);
    } else {
      return new FirefoxImpl();
    }
  }

  final boolean isWebkit() {
    return isSafari();
  }

  // Default instance methods: most return false, since they are intended to be overriden.
  boolean isSafari() {
    return false;
  }

  boolean isFirefox() {
    return false;
  }

  boolean isIE() {
    return false;
  }

  // NOTE(user): Created via deferred binding
  public static class SafariImpl extends UserAgentStaticProperties {
    @Override
    protected boolean isSafari() {
      return true;
    }
  }

  // NOTE(user): Created via deferred binding
  public static class FirefoxImpl extends UserAgentStaticProperties {
    @Override
    protected boolean isFirefox() {
      return true;
    }
  }

  // NOTE(user): Created via deferred binding
  public static class IEImpl extends UserAgentStaticProperties {
    @Override
    protected boolean isIE() {
      return true;
    }
  }
}
