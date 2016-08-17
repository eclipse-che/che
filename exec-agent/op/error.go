package op

const (
	// When decoding of operation Call body failed
	InvalidOperationBodyJsonErrorCode = 10000

	// When route for such operation doesn't exist
	NoSuchRouteErrorCode = 10001

	// When handler parameters are considered as not valid
	// this error type should be returned directly from the HandlerFunc
	InvalidParametersErrorCode = 10002

	// When error returned from the Route HandlerFunc is different from Error type
	InternalErrorCode = 10003
)

// May be returned by any of route HandlerFunc.
type Error struct {
	error `json:"-"`

	// An error code
	Code uint `json:"code"`

	// A short description of the occurred error.
	Message string `json:"message"`
}

func NewArgsError(err error) Error {
	return NewError(err, InvalidParametersErrorCode)
}

func NewError(err error, code uint) Error {
	return Error{
		error:   err,
		Code:    code,
		Message: err.Error(),
	}
}
