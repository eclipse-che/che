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
package helloworld;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AppController implements Controller
{

   @Override
   public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
      String numGuessByUser = request.getParameter("numGuess");
      String result = "";

      if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {
         result = "Congrats! The number is " + secretNum;
      }

      else if (numGuessByUser != null) {
         result = "Sorry, you failed. Try again later!";
      }

      ModelAndView view = new ModelAndView("guess_num");
      view.addObject("num", result);
      return view;
   }
}
