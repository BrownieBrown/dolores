package config

import (
	"os"
)

type ApiConfig struct {
	JwtSecret string
}

func LoadConfig() *ApiConfig {
	return &ApiConfig{JwtSecret: os.Getenv("JWT_SECRET")}
}
