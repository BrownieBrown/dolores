package utils

import (
	"errors"
	"fmt"
	"github.com/BrownieBrown/dolores/internal/config"
	"github.com/golang-jwt/jwt/v5"
	"net/http"
	"strings"
)

func ValidateAccessToken(tokenString string, cfg *config.ApiConfig) (*jwt.RegisteredClaims, error) {
	claims := &jwt.RegisteredClaims{}
	accessTokenIssuer := cfg.AccessTokenIssuer

	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}

		return []byte(cfg.JwtSecret), nil
	})

	if err != nil || !token.Valid || claims.Issuer != accessTokenIssuer {
		return nil, errors.New("invalid token")
	}

	return claims, nil
}

func ExtractTokenFromAuthHeader(r *http.Request) (string, error) {
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		return "", errors.New("bearer token not found in Authorization header")
	}

	parts := strings.Split(authHeader, " ")
	if len(parts) != 2 || strings.ToLower(parts[0]) != "bearer" {
		return "", errors.New("invalid Authorization header format")
	}

	bearerToken := parts[1]
	return bearerToken, nil
}

func ExtractAPIKeyFromAuthHeader(r *http.Request) (string, error) {
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		return "", errors.New("API key not found in Authorization header")
	}

	parts := strings.Split(authHeader, " ")
	if len(parts) != 2 || strings.ToLower(parts[0]) != "apikey" { // Ensure this matches lowercase conversion
		return "", errors.New("invalid Authorization header format")
	}

	apiKey := parts[1]
	return apiKey, nil
}
