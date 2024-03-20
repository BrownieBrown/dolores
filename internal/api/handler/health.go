package handler

import (
	"github.com/BrownieBrown/dolores/internal/config"
	"net/http"
)

type HealthHandler struct {
	Config *config.ApiConfig
}

func NewHealthHandler(cfg *config.ApiConfig) *HealthHandler {
	return &HealthHandler{Config: cfg}
}
func (hh *HealthHandler) GetHealth(w http.ResponseWriter, r *http.Request) {
	w.Header().Add("Content-Type", "text/plain; charset=utf-8")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(http.StatusText(http.StatusOK)))
}
