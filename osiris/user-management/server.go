package main

import (
	"fmt"
	"net/http"
	"os"

	"github.com/gorilla/mux"
)

func main() {

	router := mux.NewRouter()

	//router.HandleFunc("/api/user/new", CreateAccount).Methods("POST")
	//router.HandleFunc("/api/user/login", Authenticate).Methods("POST")

	//router.Use(JwtAuthentication) //attach JWT auth middleware

	//router.NotFoundHandler = NotFoundHandler

	port := os.Getenv("PORT")
	if port == "" {
		port = "8000" //localhost
	}

	fmt.Println(port)

	err := http.ListenAndServe(":"+port, router) //Launch the app, visit localhost:8000/api
	if err != nil {
		fmt.Print(err)
	}
}
