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
package org.eclipse.qa.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.Serializable;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AppController implements Controller, Serializable{

    class T{}
    interface B<T> {}
    public class A {

        private int count = 0;
        private class B {

            private int count = 7;

            public void test(){
                System.out.println(count);
            }
        }
    }

    private String privetStringTest;
    public void AppController(){}


    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception{
        final String secretNum = Integer.toString(new Random().nextInt(10));
        String numGuessByUser = request.getParameter("numGuess");
        String result ="";

        if (numGuessByUser != null && numGuessByUser.equals(secretNum)){
            result = "Congrats! The number is " + secretNum;
        }

        else if (numGuessByUser != null) {
            result = "Sorry, you failed. Try again later!";
        }

        ModelAndView view = new ModelAndView("guess_num");
        view.addObject("num", result);

        if(false){}

        String testVariable;

        if(numGuessByUser==numGuessByUser){}

        numGuessByUser=numGuessByUser;

        switch(result){
            case "one":
                break;
        }

        if (this != null) {}

        String stringNull=null;
        stringNull.equals(null);
        List  list = new ArrayList<>();
        List<String> list2 =new ArrayList<>();
        list.add(2);


        return view;
    }

    public void sample(final String aString) {

        boolean stringIsNull = null == aString;

        if (stringIsNull) {
            return;
        }

        System.out.println(aString.length());

        if(stringIsNull){
            System.out.println("Hello");
            return;
        }else
            System.out.println("Hello");
    }
}
