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


public class EmployeeHourlyWages implements Employee {

    private String employeeNumber;
    private String surname;
    private String dateOfBirth;
    private double hourlyRate;

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

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getAvrSalary(){
        return 20.8*8*hourlyRate;
    }

    public String toString(){
        return "\nEmployee with hourly wages\n"+getEmployeeNumber()
                +"\n"+getSurname()+"\n"+getDateOfBirth()+"\n"+getHourlyRate()
                +"\n"+getAvrSalary()+"\n";
    }
}
