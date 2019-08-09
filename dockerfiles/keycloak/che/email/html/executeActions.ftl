<html>
<head>
${msg("robotoFontIncludeStyle")?no_esc}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("executeActionsTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("executeActionsLetterText", realmName)}</p>
        ${msg("button", msg('executeActionsButtonText'), link)?no_esc}
        <p>${msg("emailLinkExpirationText", linkExpiration)}</p>
    </div>

    <#include "footer.ftl">
</body>
</html>
