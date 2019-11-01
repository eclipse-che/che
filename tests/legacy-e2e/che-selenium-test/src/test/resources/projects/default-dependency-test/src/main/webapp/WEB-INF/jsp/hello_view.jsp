<%--

    Copyright (c) 2012-2018 Red Hat, Inc.
    This program and the accompanying materials are made
    available under the terms of the Eclipse Public License 2.0
    which is available at https://www.eclipse.org/legal/epl-2.0/

    SPDX-License-Identifier: EPL-2.0

    Contributors:
      Red Hat, Inc. - initial API and implementation

--%>
<html>
  <body bgcolor="white">
    <div style="font-size: 120%; color: #1a53ff">
      <span>Enter number: </span><br />
      <form method="post" action="guess">
        <input type=text size="5" name="numGuess" >
        <input type=submit name="submit" value="Ok">
      </form>
    </div>
    <div>
      <%
          {
            java.lang.String attempt = (java.lang.String)request.getAttribute("num");
      %>
      <span><%=attempt%></span>
      <%
          }
      %>
    </div>
  </body>
</html>