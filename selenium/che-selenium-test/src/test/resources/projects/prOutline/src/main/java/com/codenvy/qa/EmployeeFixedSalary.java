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

public class EmployeeFixedSalary extends Empl {

    private   double fixedPayment;
    protected long   longVar;
    public    int    intVar;
    private   byte   byteVar;
    protected short  shortVar;
    public    char   charVar;
    float            floatVar ;
    boolean          boolVar;



    public double getFixedPayment() {
        return fixedPayment;
    }

    public void setFixedPayment(double fixedPayment) {
        this.fixedPayment = fixedPayment;
    }

    public double getAvrSalary(){
        return fixedPayment;
    }

    public String toString(){
        return "\nEmployee with fixed salary\n"+getEmployeeNumber()
                +"\n"+getSurname()+"\n"+getDateOfBirth()+"\n"+getFixedPayment()
                +"\n"+getAvrSalary()+"\n";
    }
}
