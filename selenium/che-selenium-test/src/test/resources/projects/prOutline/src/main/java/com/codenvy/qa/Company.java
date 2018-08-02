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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Company {

    private Company() {
    }

    private static class CompanyHelper {
        private static final Company INSTANCE = new Company();
        private static final int     ONE      = 1;
        private static final String  QWE      = "qwe";
        private static final double  TWO      = 2.0;
    }

    private interface Inter {
        String ASD  = "asd";
        int    FIVE = 5;
        double TEN  = 10.0;

        public void   setDate();
        public double getId();
        public String getDate();
    }

    public static Company getInstance(){
        return CompanyHelper.INSTANCE;
    }

    List<Employee> listEmployees = new ArrayList();
    List <String> listId = new ArrayList();
    List <String> listName = new ArrayList();
    List <String> listDate = new ArrayList();

    public List <String> doListId(){
        listId.add("A319");
        listId.add("B012");
        listId.add("A009");
        listId.add("A023");
        listId.add("F018");
        listId.add("C541");
        listId.add("M119");
        listId.add("P763");
        listId.add("D106");
        listId.add("H524");
        listId.add("G009");
        listId.add("Z333");
        listId.add("Y555");
        listId.add("X999");
        listId.add("Q777");

        return listId;
    }

    public List <String> doListName(){
        listName.add("Antonov");
        listName.add("Ivanov");
        listName.add("Petrov");
        listName.add("Pupkin");
        listName.add("Husev");
        listName.add("Sidorov");
        listName.add("Zaycev");
        listName.add("Svetlov");
        listName.add("Lebedev");
        listName.add("Mironov");
        listName.add("Orlov");
        listName.add("Papanov");
        listName.add("Davydov");
        listName.add("Mashkov");
        listName.add("Shatrov");

        return listName;
    }

    public List <String> doListDate(){
        listDate.add("1967/10/04");
        listDate.add("1979/12/15");
        listDate.add("1989/07/19");
        listDate.add("1996/02/29");
        listDate.add("1985/08/08");
        listDate.add("1990/03/05");
        listDate.add("1965/11/09");
        listDate.add("1956/07/06");
        listDate.add("1986/05/16");
        listDate.add("1987/07/19");
        listDate.add("1966/01/06");
        listDate.add("1979/10/21");
        listDate.add("1976/10/14");
        listDate.add("1970/04/12");
        listDate.add("1980/06/18");

        return listDate;
    }

    public List <Employee> createListEmpl()throws InvalidArgumentException {
        listEmployees.clear();
        int numEmployees = (int) (Math.random() * 14 + 1);
        Collections.shuffle(doListId());
        Collections.shuffle(doListName());
        Collections.shuffle(doListDate());
        for(int i=0; i<= numEmployees; i++){
            switch ((int) (Math.random() * 2)){
                case 0:
                    double rndFixedPayment = 500+(int)(Math.random()*9510);
                    listEmployees.add((Employee) Initializer.createEmpFixed(listId.get(i),listName.get(i),
                            listDate.get(i),rndFixedPayment));
                    break;

                case  1:
                    double rndHourlyRate = 5+(int)(Math.random()*96);
                    listEmployees.add(Initializer.createEmpHourly(listId.get(i),listName.get(i),
                            listDate.get(i),rndHourlyRate));
                    break;
            }
        }
        return listEmployees;
    }

    public List <Employee> createListEmpl(int numEmployees)throws InvalidArgumentException {
        listEmployees.clear();
        Collections.shuffle(doListId());
        Collections.shuffle(doListName());
        Collections.shuffle(doListDate());
        numEmployees = numEmployees-1;
        for(int i=0; i<= numEmployees; i++){
            switch ((int) (Math.random() * 2)){
                case 0:
                    double rndFixedPayment = 500+(int)(Math.random()*9510);
                    listEmployees.add((Employee) Initializer.createEmpFixed(listId.get(i),listName.get(i),
                            listDate.get(i),rndFixedPayment));
                    break;

                case  1:
                    double rndHourlyRate = 5+(int)(Math.random()*96);
                    listEmployees.add(Initializer.createEmpHourly(listId.get(i),listName.get(i),
                            listDate.get(i),rndHourlyRate));
                    break;
            }
        }
        return listEmployees;
    }

    public List<Employee> removeEmployee(String surName){
        for(Employee e:listEmployees){
            if(e.getSurname().equals(surName)){
                listEmployees.remove(e);
                break;
            }
        }
        return listEmployees;
    }

    public List<Employee> getListEmployees(){
        return listEmployees;
    }

    public List<Employee> sortSalary() throws IllegalAccessException, InstantiationException {
        Collections.sort(listEmployees,SortSalary.class.newInstance());
        return listEmployees;
    }
    public List<Employee> sortSurname() throws IllegalAccessException, InstantiationException {
        Collections.sort(listEmployees,SortSurname.class.newInstance());
        return  listEmployees;
    }

    public List<Employee> sortId() throws IllegalAccessException, InstantiationException {
        Collections.sort(listEmployees,SortId.class.newInstance());
        return listEmployees;
    }

    public List<Employee> sortDate() throws IllegalAccessException, InstantiationException {
        Collections.sort(listEmployees,SortDate.class.newInstance());
        return listEmployees;
    }
}
