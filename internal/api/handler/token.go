package handler

import (
	"errors"
	"fmt"
	"github.com/BrownieBrown/dolores/internal/models"
	"github.com/BrownieBrown/dolores/internal/utils"
	"github.com/golang-jwt/jwt/v5"
	"net/http"
	"strconv"
	"strings"
	"time"
)

func (uh *UserHandler) RefreshToken(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	tokenString, err := extractTokenFromAuthHeader(r)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	claims, err := uh.validateRefreshToken(tokenString)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	userID, err := strconv.Atoi(claims.Subject)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, "Failed to parse token")
		return
	}

	newToken, err := uh.generateAccessToken(userID)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to generate new token")
		return
	}

	response := models.RefreshTokenResponse{
		AccessToken: newToken,
	}

	utils.WriteData(w, http.StatusOK, response)
}

func (uh *UserHandler) InvalidateRefreshToken(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	tokenString, err := extractTokenFromAuthHeader(r)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	_, err = uh.validateRefreshToken(tokenString)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	err = uh.Database.InvalidateRefreshToken(tokenString)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to invalidate token")
		return
	}

	utils.WriteData(w, http.StatusOK, nil)
}

func (uh *UserHandler) validateAccessToken(tokenString string) (*jwt.RegisteredClaims, error) {
	claims := &jwt.RegisteredClaims{}
	accessTokenIssuer := "chirpy-access"

	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}

		return []byte(uh.Config.JwtSecret), nil
	})

	if err != nil || !token.Valid || claims.Issuer != accessTokenIssuer {
		return nil, errors.New("invalid token")
	}

	return claims, nil
}

func (uh *UserHandler) validateRefreshToken(tokenString string) (*jwt.RegisteredClaims, error) {
	claims := &jwt.RegisteredClaims{}
	refreshTokenIssuer := "chirpy-refresh"

	if uh.Database.RefreshTokenIsInvalid(tokenString) {
		return nil, errors.New("invalid token")
	}

	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}

		return []byte(uh.Config.JwtSecret), nil
	})

	if err != nil || !token.Valid || claims.Issuer != refreshTokenIssuer {
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

func (uh *UserHandler) generateAccessToken(userID int) (string, error) {
	issuer := "chirpy-access"
	method := jwt.SigningMethodHS256
	subject := fmt.Sprintf("%d", userID)
	issuedAt := jwt.NewNumericDate(time.Now())

	expiresAt := jwt.NewNumericDate(time.Now().Add(time.Hour * 1))

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

func (uh *UserHandler) generateRefreshToken(userID int) (string, error) {
	issuer := "chirpy-refresh"
	method := jwt.SigningMethodHS256
	subject := fmt.Sprintf("%d", userID)
	issuedAt := jwt.NewNumericDate(time.Now())

	expiresAt := jwt.NewNumericDate(time.Now().Add(time.Hour * 24 * 60))

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
