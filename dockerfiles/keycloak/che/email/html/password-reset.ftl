<html>
<head>
${msg("robotoFontIncludeStyle")}
</head>
<body style="${msg("mailBodyStyle")}">
    <#assign letterTitle=msg("passwordResetTitle")>
    <#include "header.ftl">

    <div style="${msg("mailContentStyle")}">
        <p>${msg("passwordResetLetterText", realmName)}</p>
        ${msg("button", msg('passwordResetButtonText'), link)}
        <p>${msg("emailLinkExpirationText", linkExpiration)}</p>
    </div>

    <#include "footer.ftl">
</body>
</html>
