package config

import (
	"os"
)

type ApiConfig struct {
	JwtSecret          string
	AccessTokenIssuer  string
	RefreshTokenIssuer string
	PolkaAPIKey        string
}

func LoadConfig() *ApiConfig {
	return &ApiConfig{
		JwtSecret:          os.Getenv("JWT_SECRET"),
		AccessTokenIssuer:  os.Getenv("ACCESS_TOKEN_ISSUER"),
		RefreshTokenIssuer: os.Getenv("REFRESH_TOKEN_ISSUER"),
		PolkaAPIKey:        os.Getenv("POLKA_API_KEY"),
	}
}
