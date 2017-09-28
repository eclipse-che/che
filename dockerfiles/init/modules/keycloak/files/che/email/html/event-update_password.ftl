<html>
<head>
${msg("robotoFontIncludeStyle")}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("eventUpdatePasswordTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("eventUpdatePasswordLetterText",event.date, event.ipAddress)}</p>
    </div>

    <#include "footer.ftl">
</body>
</html>
