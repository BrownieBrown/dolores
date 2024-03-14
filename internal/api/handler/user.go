package handler

import (
	"encoding/json"
	"github.com/BrownieBrown/dolores/internal/database"
	"github.com/BrownieBrown/dolores/internal/models"
	"github.com/BrownieBrown/dolores/internal/utils"
	"golang.org/x/crypto/bcrypt"
	"net/http"
)

type UserHandler struct {
	Database *database.DB
}

func NewUserHandler(database *database.DB) *UserHandler {
	return &UserHandler{
		Database: database,
	}
}

func (uh *UserHandler) SignUp(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	var loginReq models.SignUpRequest
	if err := json.NewDecoder(r.Body).Decode(&loginReq); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	newUser, err := uh.Database.CreateUser(loginReq)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to create user")
		return
	}

	utils.WriteData(w, http.StatusCreated, models.SignUpResponse{ID: newUser.ID, Email: newUser.Email})
}

func (uh *UserHandler) SignIn(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	var loginReq models.SignInRequest
	if err := json.NewDecoder(r.Body).Decode(&loginReq); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	user, err := uh.Database.GetUserByEmail(loginReq.Email)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	if err := bcrypt.CompareHashAndPassword(user.Password, []byte(loginReq.Password)); err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	signInResponse := models.SignInResponse{
		Email: user.Email,
		ID:    user.ID,
	}

	utils.WriteData(w, http.StatusOK, signInResponse)
}
