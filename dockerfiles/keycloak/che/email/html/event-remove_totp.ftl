<html>
<head>
${msg("robotoFontIncludeStyle")?no_esc}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("eventRemoveTotpTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("eventRemoveTotpLetterText", event.date, event.ipAddress)}</p>
    </div>

    <#include "footer.ftl">
</body>
</html>
