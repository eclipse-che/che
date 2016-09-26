// Moved from term/server.go
package auth

import (
	"errors"
	"github.com/eclipse/che/exec-agent/rest"
	"net/http"
)

var (
	Enabled     = false
	ApiEndpoint string
)

func AuthenticateOnMaster(r *http.Request) error {
	tokenParam := r.URL.Query().Get("token")
	if tokenParam == "" {
		return rest.Unauthorized(errors.New("Authentication failed: missing 'token' query parameter"))
	}
	req, err := http.NewRequest("GET", ApiEndpoint+"/machine/token/user/"+tokenParam, nil)
	if err != nil {
		return rest.Unauthorized(err)
	}
	req.Header.Add("Authorization", tokenParam)
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return rest.Unauthorized(err)
	}
	if resp.StatusCode != 200 {
		return rest.Unauthorized(errors.New("Authentication failed, token: %s is invalid"))
	}
	return nil
}
