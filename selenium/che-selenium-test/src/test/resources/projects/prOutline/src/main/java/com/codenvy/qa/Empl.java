/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.qa;

import java.io.Serializable;

public abstract class Empl implements Serializable {

    private String employeeNumber;
    private String surname;
    private String dateOfBirth;

    public abstract double getAvrSalary();
    public abstract String toString();

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setSurname(String surname) {

        this.surname = surname;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getSurname() {
        return surname;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}
