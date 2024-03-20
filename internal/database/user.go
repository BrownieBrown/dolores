package database

import (
	"errors"
	"github.com/BrownieBrown/dolores/internal/models"
	"golang.org/x/crypto/bcrypt"
)

func (db *DB) emailExists(email string) bool {
	dbContent, err := db.loadDB()
	if err != nil {
		return false
	}

	for _, user := range dbContent.Users {
		if user.Email == email {
			return true
		}
	}

	return false
}

func (db *DB) validateSignUpRequest(signupReq models.SignUpRequest) error {
	if signupReq.Email == "" {
		return errors.New("email required")
	}

	if db.emailExists(signupReq.Email) {
		return errors.New("email already exists")
	}

	if signupReq.Password == "" {
		return errors.New("password required")
	}

	return nil
}

func (db *DB) CreateUser(signupReq models.SignUpRequest) (models.User, error) {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return models.User{}, err
	}

	err = db.validateSignUpRequest(signupReq)
	if err != nil {
		return models.User{}, err
	}

	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(signupReq.Password), bcrypt.DefaultCost)
	if err != nil {
		return models.User{}, err

	}

	lastUserID := len(dbContent.Users)
	newUser := models.User{ID: lastUserID + 1, Email: signupReq.Email, Password: hashedPassword, PremiumMember: false}
	dbContent.Users[newUser.ID] = newUser

	if err = db.writeDB(dbContent); err != nil {
		return models.User{}, err
	}

	return newUser, nil
}

func (db *DB) UpdateUser(user models.User) error {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return err
	}

	dbContent.Users[user.ID] = user

	if err = db.writeDB(dbContent); err != nil {
		return err
	}

	return nil
}

func (db *DB) searchUserByEmail(email string) (models.User, error) {
	dbContent, err := db.loadDB()
	if err != nil {
		return models.User{}, err
	}

	for _, user := range dbContent.Users {
		if user.Email == email {
			return user, nil
		}
	}

	return models.User{}, errors.New("user not found")
}

func (db *DB) GetUserByEmail(email string) (models.User, error) {
	user, err := db.searchUserByEmail(email)
	if err != nil {
		return models.User{}, err
	}

	return user, nil
}

func (db *DB) GetUserByID(id int) (models.User, error) {
	dbContent, err := db.loadDB()
	if err != nil {
		return models.User{}, err
	}

	user, ok := dbContent.Users[id]
	if !ok {
		return models.User{}, errors.New("user not found")
	}

	return user, nil
}
