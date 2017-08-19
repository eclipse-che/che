/**
 * ***************************************************************************** Copyright (c) 2017
 * Red Hat, Inc. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.console;

/**
 * An interface to allow output text customizations
 *
 * @author Victor Rubezhny
 */
public interface OutputCustomizer {
  /** Checks if the specified text can be/has to be customized */
  boolean canCustomize(String text);

  /** Returns the result of customization for the specified text */
  String customize(String text);
}
