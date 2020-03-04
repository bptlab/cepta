package models

import (
	"testing"

	"github.com/jinzhu/gorm"
	mocket "github.com/selvatico/go-mocket"
	"golang.org/x/crypto/bcrypt"
)

func SetupTests() { // or *gorm.DB
	mocket.Catcher.Register() // Safe register. Allowed multiple calls to save
	// GORM
	db, err := gorm.Open(mocket.DriverName, "connection_string") // Can be any connection string
	if err != nil {
		print(err)
	}
	DB = db

}

// Test function
func TestCorrectLogin(t *testing.T) {
	SetupTests()
	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
	testPassword := string(hashedPassword)
	commonReply := []map[string]interface{}{{"email": "email", "password": testPassword}}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
	result := Login("email", "password")
	if (result["message"]) != "Logged In" {
		t.Errorf("Not Logged In correctly! Got '%v'", result["message"])
	}
}

func TestWrongEmailLogin(t *testing.T) {
	SetupTests()
	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
	testPassword := string(hashedPassword)
	commonReply := []map[string]interface{}{{"email": "test", "password": testPassword}}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
	result := Login("email1", "password")
	if (result["message"]) != "Email address not found" {
		t.Errorf("Message should be: Email address not found. Got '%v'", result["message"])
	}
}

func TestWrongPasswordLogin(t *testing.T) {
	SetupTests()
	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
	testPassword := string(hashedPassword)
	commonReply := []map[string]interface{}{{"email": "test", "password": testPassword}}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
	result := Login("email", "password1")
	if (result["message"]) != "Invalid login credentials. Please try again" {
		t.Errorf("Message should be: Invalid login credentials. Got '%v'", result["message"])
	}
}
func TestGetUser(t *testing.T) {
	SetupTests()
	commonReply := []map[string]interface{}{{"email": "test", "password": "password"}}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((id = 1)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
	result := GetUser(1)
	if result.Email != "test" {
		t.Errorf("Email is not correct! Got '%v'", result.Email)
	}
	if result.Password != "" {
		t.Errorf("Secret is displayed! Thats not correct! Got '%v'", result.Password)
	}
}

func TestCreateAccount(t *testing.T) {
	SetupTests()
	testAccount := Account{Email: "email@email", Password: "password"}
	result := testAccount.Create()
	if (result["message"]) != "Account has been created" && result["status"] != true {
		t.Errorf("Message should be: Account has been created. Got '%v'", result["message"])
	}
}

func TestWrongEmailValidateAccount(t *testing.T) {
	SetupTests()
	testAccount := Account{Email: "emailWithoutAt", Password: "password"}
	result, _ := testAccount.Validate()
	if (result["message"]) != "Email address is required" && result["status"] != false {
		t.Errorf("Message should be: Email address is required. Got '%v'", result["message"])
	}
}

func TestWrongPasswordValidateAccount(t *testing.T) {
	SetupTests()
	testAccount := Account{Email: "email@email", Password: "pass"}
	result, _ := testAccount.Validate()
	if (result["message"]) != "Password is required" && result["status"] != false {
		t.Errorf("Message should be: Password address is required. Got '%v'", result["message"])
	}
}

func TestAlreadyTakenEmailValidateAccount(t *testing.T) {
	SetupTests()
	testAccount := Account{Email: "emailTaken@email", Password: "password"}
	commonReply := []map[string]interface{}{{"email": "emailTaken@email", "password": "passowrd"}}
	mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = emailTaken@email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
	result, _ := testAccount.Validate()
	if (result["message"]) != "Email address already in use by another user." && result["status"] != false {
		t.Errorf("Message should be: Email address already in use by another user. Got '%v'", result["message"])
	}
}

func TestValidateCorrectAccount(t *testing.T) {
	SetupTests()
	testAccount := Account{Email: "emailSuccess@email", Password: "password"}
	result, _ := testAccount.Validate()
	if (result["message"]) != "Requirement passed" && result["status"] != true {
		t.Errorf("Message should be: Requirement passed. Got '%v'", result["message"])
	}
}
