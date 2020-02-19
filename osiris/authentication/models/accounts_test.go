package models

import (
	"testing"

	mocket "github.com/Selvatico/go-mocket"
	"github.com/jinzhu/gorm"
	"golang.org/x/crypto/bcrypt"
)

func SetupTests() { // or *gorm.DB
	mocket.Catcher.Register() // Safe register. Allowed multiple calls to save
	mocket.Catcher.Logging = true
	// GORM
	db, err := gorm.Open(mocket.DriverName, "connection_string") // Can be any connection string
	if err != nil {
		print(err)
	}
	DB = db

}

// Test function
func TestGetAndLogin(t *testing.T) {
	SetupTests()
	t.Run("Test GetUser", func(t *testing.T) {
		mocket.Catcher.Logging = true
		//	testAccount := Account{Email: "test"}
		commonReply := []map[string]interface{}{{"email": "test", "password": "password"}}
		mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((id = 1)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
		result := GetUser(1)
		if result.Email != "test" {
			t.Errorf("Email is not correct! Got '%v'", result.Email)
		}
		if result.Password != "" {
			t.Errorf("Secret is displayed! Thats not correct! Got '%v'", result.Password)
		}
	})

	t.Run("Test Correct Login", func(t *testing.T) {
		mocket.Catcher.Logging = true
		hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
		testPassword := string(hashedPassword)
		commonReply := []map[string]interface{}{{"email": "test", "password": testPassword}}
		mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
		result := Login("email", "password")
		if (result["message"]) != "Logged In" {
			t.Errorf("Not Logged In correctly! Got '%v'", result["message"])
		}
	})

	t.Run("Test Wrong Email Login", func(t *testing.T) {
		mocket.Catcher.Logging = true
		hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
		testPassword := string(hashedPassword)
		commonReply := []map[string]interface{}{{"email": "test", "password": testPassword}}
		mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
		result := Login("email1", "password")
		if (result["message"]) != "Email address not found" {
			t.Errorf("Message should be: Email address not found. Got '%v'", result["message"])
		}
	})

	t.Run("Test Wrong Password Login", func(t *testing.T) {
		mocket.Catcher.Logging = true
		hashedPassword, _ := bcrypt.GenerateFromPassword([]byte("password"), bcrypt.DefaultCost)
		testPassword := string(hashedPassword)
		commonReply := []map[string]interface{}{{"email": "test", "password": testPassword}}
		mocket.Catcher.Reset().NewMock().WithQuery(`SELECT * FROM "accounts"  WHERE "accounts"."deleted_at" IS NULL AND ((email = email)) ORDER BY "accounts"."id" ASC LIMIT 1`).WithReply(commonReply)
		result := Login("email", "password1")
		if (result["message"]) != "Invalid login credentials. Please try again" {
			t.Errorf("Message should be: Invalid login credentials. Got '%v'", result["message"])
		}
	})

}

func TestCreate(t *testing.T) {
	SetupTests()
	t.Run("Test Create User", func(t *testing.T) {
		mocket.Catcher.Logging = true
		testAccount := Account{Email: "email@email", Password: "password"}
		result := testAccount.Create()
		if (result["message"]) != "Account has been created" && result["status"] != true {
			t.Errorf("Message should be: Account has been created. Got '%v'", result["message"])
		}
	})
}

func TestValidate(t *testing.T) {
	SetupTests()
	t.Run("Test Correct Validate User", func(t *testing.T) {
		mocket.Catcher.Logging = true
		testAccount := Account{Email: "email@email", Password: "password"}
		result, _ := testAccount.Validate()
		if (result["message"]) != "Requirement passed" && result["status"] != true {
			t.Errorf("Message should be: Requirement passed. Got '%v'", result["message"])
		}
	})
}
