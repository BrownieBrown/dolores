package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/BrownieBrown/dolores/internal/config"
	"github.com/BrownieBrown/dolores/internal/database"
	"github.com/BrownieBrown/dolores/internal/models"
	"github.com/BrownieBrown/dolores/internal/utils"
	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
	"net/http"
	"strconv"
	"strings"
	"time"
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

	token, err := uh.generateToken(user.ID, loginReq.ExpiresInSeconds)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to generate token")
		return
	}

	signInResponse := models.SignInResponse{
		Email: user.Email,
		ID:    user.ID,
		Token: token,
	}

	utils.WriteData(w, http.StatusOK, signInResponse)
}

func (uh *UserHandler) UpdateUser(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPut {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	tokenString, err := extractTokenFromAuthHeader(r)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	claims, err := uh.validateToken(tokenString)
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

func (uh *UserHandler) validateToken(tokenString string) (*jwt.RegisteredClaims, error) {
	claims := &jwt.RegisteredClaims{}

	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}

		return []byte(uh.Config.JwtSecret), nil
	})

	if err != nil || !token.Valid {
		return nil, errors.New("invalid token")
	}

	return claims, nil
}

func extractTokenFromAuthHeader(r *http.Request) (string, error) {
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		return "", errors.New("bearer token not found in Authorization header")
	}

	parts := strings.Split(authHeader, " ")
	if len(parts) != 2 || strings.ToLower(parts[0]) != "bearer" {
		return "", errors.New("invalid Authorization header format")
	}

	return parts[1], nil
}

func (uh *UserHandler) generateToken(userID int, expiresInSeconds *int) (string, error) {
	issuer := "chirpy"
	method := jwt.SigningMethodHS256
	subject := fmt.Sprintf("%d", userID)
	issuedAt := jwt.NewNumericDate(time.Now())

	var expiresAt *jwt.NumericDate
	if expiresInSeconds != nil {
		expiresAt = jwt.NewNumericDate(time.Now().Add(time.Second * time.Duration(*expiresInSeconds)))
	}

	claims := jwt.RegisteredClaims{
		Issuer:    issuer,
		Subject:   subject,
		IssuedAt:  issuedAt,
		ExpiresAt: expiresAt,
	}

	jwtToken := jwt.NewWithClaims(method, claims)
	signedToken, err := jwtToken.SignedString([]byte(uh.Config.JwtSecret))
	if err != nil {
		return "", err
	}

	return signedToken, nil
}
