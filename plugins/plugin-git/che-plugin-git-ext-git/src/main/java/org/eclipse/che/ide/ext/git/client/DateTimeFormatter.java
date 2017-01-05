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
package org.eclipse.che.ide.ext.git.client;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * This class is used for formatting of date on the client side.
 *
 * @author Sergii Leschenko
 */
public class DateTimeFormatter {
    DateTimeFormat formatter = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);

    public String getFormattedDate(long time) {
        return getFormattedDate(new Date(time));
    }

    public String getFormattedDate(Date date) {
        return formatter.format(date);
    }
}
