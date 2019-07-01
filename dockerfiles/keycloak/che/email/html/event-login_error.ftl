<html>
<head>
${msg("robotoFontIncludeStyle")}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("eventLoginErrorTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("eventLoginErrorLetterText", event.date, event.ipAddress)}</p>        
    </div>

    <#include "footer.ftl">
</body>
</html>
