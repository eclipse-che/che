/*******************************************************************************
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.shared;

/**
 * @author Kaloyan Raev
 */
public class Constants {

    public final static String COMPOSER_PROJECT_TYPE_ID = "composer";
    public final static String PACKAGE                  = "package";

    public final static String COMPOSER_CHANNEL_OUTPUT      = "composer/output";
    public final static String COMPOSER_CHANNEL_SUBSCRIBE   = "composer/subscribe";
    public final static String COMPOSER_CHANNEL_UNSUBSCRIBE = "composer/unsubscribe";

    private Constants() {
    }

}
