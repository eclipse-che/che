/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.qa;

import java.io.Serializable;

public interface Employee extends Serializable {

    public double getAvrSalary();
    public String toString();
    public String getEmployeeNumber();
    public String getSurname();
    public String getDateOfBirth();
}
