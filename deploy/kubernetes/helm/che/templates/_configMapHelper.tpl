{{- define "properties" -}}
  {{- range $key, $value := . -}}
    {{- $key | upper | replace "_" "__" | nindent 2 -}}
    {{- template "travarsalProperty" . }}
  {{- end -}}
{{- end -}}
{{- define "travarsalProperty" -}}
  {{- if (eq (kindOf .) "map") -}}
    _ {{- template "_properties" . -}}
  {{- else -}}
    {{- printf ": %s" (quote .) -}}
  {{- end -}}
{{- end -}}
{{- define "_properties" -}}
  {{- range $key, $value := . -}}
    {{- $key | upper | replace "_" "__" -}}
    {{- template "travarsalProperty" . -}}
  {{- end -}}
{{- end -}}
