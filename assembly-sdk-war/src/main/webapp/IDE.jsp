<%--

    Copyright (c) 2012-2015 Codenvy, S.A.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
      Codenvy, S.A. - initial API and implementation

--%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="mobile-web-app-capable" content="yes">		
    <title>Eclipse Che</title>
    <link rel="shortcut icon" href="/che/_app/favicon.ico"/>
    <link href="http://fonts.googleapis.com/css?family=Droid+Sans+Mono" rel="stylesheet" type="text/css" />

    <script type="text/javascript" language="javascript">

        /**This parameter is needed to define sdk mode.*/
        window.sdk = 1;

        /**
         * Base IDE object
         */

        window.IDE = {};

        /**
         * Initial configuration
         */

        window.IDE.config = {

            "context": "/che",

            "restContext": "/che/api",

            "workspaceName": "<%= pageContext.getServletContext().getInitParameter("ws-name") %>",

            "workspaceId": "<%= pageContext.getServletContext().getInitParameter("ws-id") %>",

            "projectName": window.location.pathname.split("/")[3] ? window.location.pathname.split("/")[3] : null,

            "startupParams": location.search ? location.search.substring(1) : null,

            "hiddenFiles": ".*",

            "javaCodeAssistant": "/che/api"

        };

        /**
         * Event handlers
         */

        window.IDE.eventHandlers = {};

        window.IDE.eventHandlers.initializationFailed = function(message) {
            if (message) {
                var err = new Error(message);
                window.alert(err.stack);
            } else {
                window.alert("Unable to initialize IDE");
            }
        };

        /**
         * Make URL pretty
         */

        window.history.pushState("", window.document.title, window.IDE.config.context + "/" + window.IDE.config.workspaceName);

    </script>

    <script type="text/javascript" language="javascript" src="/che/_app/browserNotSupported.js"></script>
    <script type="text/javascript" language="javascript" src="/che/_app/_app.nocache.js"></script>
</head>
</html>
