<html>
<head>
${msg("robotoFontIncludeStyle")?no_esc}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("eventUpdateTotpTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("eventUpdateTotpLetterText",event.date, event.ipAddress)}</p>
    </div>

    <#include "footer.ftl">
</body>
</html>
