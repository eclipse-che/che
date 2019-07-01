<html>
<head>
${msg("robotoFontIncludeStyle")}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("executeActionsTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("executeActionsLetterText", realmName)}</p>
        ${msg("button", msg('executeActionsButtonText'), link)}
        <p>${msg("emailLinkExpirationText", linkExpiration)}</p>
    </div>

    <#include "footer.ftl">
</body>
</html>
