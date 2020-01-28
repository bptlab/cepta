package main

import (
	"net/http"

	u "github.com/bptlab/cepta/osiris/authentication/utils"
)

// NotFoundHandler handles non existing ressources on server
var NotFoundHandler = func(next http.Handler) http.Handler {

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNotFound)
		u.Respond(w, u.Message(false, "This resources was not found on our server"))
		next.ServeHTTP(w, r)
	})
}
