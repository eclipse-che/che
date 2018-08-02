/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.qa;


public class Initializer  {


    /**
     * Creates, initializes, and returns a new object EmployeeFixedSalary
     *
     * @param employeeNumber the EmployeeFixedSalary's ID. This may be any non-null String
     * @param surname the EmployeeFixedSalary's last name. This may be any non-null String
     * @param dateOfBirth the EmployeeFixedSalary's date of birth. This may be any non-null String
     * @param fixedPayment the EmployeeFixedSalary's fixed payment. This may be any double in the range 500.0 to 10000.0
     * @return a reference to the new EmployeeFixedSalary object
     * @throws InvalidArgumentException if any of the input parameters is invalid
     */

    public static EmployeeFixedSalary createEmpFixed(String employeeNumber,
                                                     String surname,
                                                     String dateOfBirth,
                                                     double fixedPayment)throws InvalidArgumentException {

        EmployeeFixedSalary empFixed = new EmployeeFixedSalary() ;

        if ((employeeNumber != null)&&(!employeeNumber.equals("")))
            empFixed.setEmployeeNumber(employeeNumber);
        else
            throw new InvalidArgumentException();

        if ((surname != null)&&(!surname.equals("")))
            empFixed.setSurname(surname);
        else
            throw new InvalidArgumentException();

        if ((dateOfBirth != null)&&(!dateOfBirth.equals("")))
            empFixed.setDateOfBirth(dateOfBirth);
        else
            throw new InvalidArgumentException();

        if (fixedPayment >= 500 && fixedPayment <=10000)
            empFixed.setFixedPayment( fixedPayment);
        else
            throw new InvalidArgumentException();

        empFixed.getAvrSalary();

        return empFixed;
    }

    /**
     * Creates, initializes, and returns a new object the EmployeeHourlyWages
     *
     * @param employeeNumber the EmployeeHourlyWages's ID.  This may be any non-null String
     * @param surname the EmployeeHourlyWages's last name.  This may be any non-null String
     * @param dateOfBirth the EmployeeHourlyWages's date of birth.  This may be any non-null String
     * @param hourlyRate the EmployeeHourlyWages's hourly rate.This may be any double in the range 5.0 to 100.0
     * @return a reference to the new EmployeeHourlyWages object
     * @throws InvalidArgumentException if any of the input parameters is invalid
     */

    public static Employee createEmpHourly(String employeeNumber,
                                           String surname,
                                           String dateOfBirth,
                                           double hourlyRate)throws InvalidArgumentException {

        EmployeeHourlyWages empHourly = new EmployeeHourlyWages();

        if ((employeeNumber != null)&&(!employeeNumber.equals("")))
            empHourly.setEmployeeNumber(employeeNumber);
        else
            throw new InvalidArgumentException();

        if ((surname != null)&&(!surname.equals("")))
            empHourly.setSurname(surname);
        else
            throw new InvalidArgumentException();

        if ((dateOfBirth != null)&&(!dateOfBirth.equals("")))
            empHourly.setDateOfBirth(dateOfBirth);
        else
            throw new InvalidArgumentException();

        if (hourlyRate >=5.0 && hourlyRate <=100.0)
            empHourly.setHourlyRate(hourlyRate);
        else
            throw new InvalidArgumentException();

        empHourly.getAvrSalary();

        return empHourly;
    }
}
