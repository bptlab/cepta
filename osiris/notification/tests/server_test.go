package main

import (
  "testing"
)

const parallel = true

func TestLogin(t *testing.T) {
	test := new(Test).setup(t)
	defer test.teardown()

}

func TestWebsocket(t *testing.T) {
    test := new(Test).setup(t)
    defer test.teardown()

    test.websocketHelper(t)
}
