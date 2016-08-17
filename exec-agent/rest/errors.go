package rest

import (
	"net/http"
)

type ApiError struct {
	error
	Code int
}

func BadRequest(err error) error {
	return ApiError{err, http.StatusBadRequest}
}

func NotFound(err error) error {
	return ApiError{err, http.StatusNotFound}
}

func Conflict(err error) error {
	return ApiError{err, http.StatusConflict}
}

func Forbidden(err error) error {
	return ApiError{err, http.StatusForbidden}
}

func Unauthorized(err error) error {
	return ApiError{err, http.StatusUnauthorized}
}
