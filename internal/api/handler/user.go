package handler

import (
	"encoding/json"
	"github.com/BrownieBrown/dolores/internal/config"
	"github.com/BrownieBrown/dolores/internal/database"
	"github.com/BrownieBrown/dolores/internal/models"
	"github.com/BrownieBrown/dolores/internal/utils"
	"golang.org/x/crypto/bcrypt"
	"net/http"
	"strconv"
)

type UserHandler struct {
	Config   *config.ApiConfig
	Database *database.DB
}

func NewUserHandler(cfg *config.ApiConfig, database *database.DB) *UserHandler {
	return &UserHandler{
		Config:   cfg,
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

	accessToken, err := uh.generateAccessToken(user.ID)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to generate accessToken")
		return
	}

	refreshToken, err := uh.generateRefreshToken(user.ID)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to generate refreshToken")
		return
	}

	signInResponse := models.SignInResponse{
		Email:        user.Email,
		ID:           user.ID,
		Token:        accessToken,
		RefreshToken: refreshToken,
	}

	utils.WriteData(w, http.StatusOK, signInResponse)
}

func (uh *UserHandler) UpdateUser(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPut {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	tokenString, err := utils.ExtractTokenFromAuthHeader(r)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	claims, err := utils.ValidateAccessToken(tokenString, uh.Config)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	id, err := strconv.Atoi(claims.Subject)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, "Invalid token")
		return

	}

	user, err := uh.Database.GetUserByID(id)
	if err != nil {
		utils.WriteError(w, http.StatusNotFound, "User not found")
		return
	}

	var updateRequest models.UpdateUserRequest

	if err := json.NewDecoder(r.Body).Decode(&updateRequest); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	user.Email = updateRequest.Email
	user.Password, err = bcrypt.GenerateFromPassword([]byte(updateRequest.Password), bcrypt.DefaultCost)

	if err := uh.Database.UpdateUser(user); err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to update user")
		return
	}

	response := models.UpdateUserResponse{
		ID:    user.ID,
		Email: user.Email,
	}

	utils.WriteData(w, http.StatusOK, response)
}
