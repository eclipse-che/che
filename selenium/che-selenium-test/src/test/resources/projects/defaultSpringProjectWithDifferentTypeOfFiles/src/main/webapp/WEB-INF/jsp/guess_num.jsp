<%--

    Copyright (c) 2012-2017 Codenvy, S.A.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
      Codenvy, S.A. - initial API and implementation

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
