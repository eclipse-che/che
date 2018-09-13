{{- define "workspaceServiceAccountName" }}
{{- if (.Values.global.cheWorkspacesNamespace) }}
{{- printf "che-workspace" }}
{{- end }}
{{- end }}
