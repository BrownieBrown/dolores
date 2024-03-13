package router

import (
	"github.com/BrownieBrown/dolores/internal/api/handler"
	"github.com/BrownieBrown/dolores/internal/api/middleware"
	"net/http"
)

type Router struct {
	*http.ServeMux
}

func NewRouter() *Router {
	return &Router{http.NewServeMux()}
}

func (r *Router) Init(cfg *middleware.ApiConfig, ch *handler.ChirpHandler, hh *handler.HealthHandler) {
	fileServerHandler := http.StripPrefix("/app/", http.FileServer(http.Dir(".")))
	r.Handle("/app/", cfg.IncrementFileServerHits(fileServerHandler))

	assetHandler := http.StripPrefix("/app/assets/", http.FileServer(http.Dir("./assets")))
	r.Handle("/app/assets/", cfg.IncrementFileServerHits(assetHandler))

	r.HandleFunc("GET /api/healthz", hh.GetHealth)

	r.HandleFunc("GET /admin/metrics", cfg.GetFileServerHits)

	r.HandleFunc("GET /api/reset", cfg.ResetFileServerHits)

	r.HandleFunc("POST /api/chirps", ch.CreateChirp)
	r.HandleFunc("GET /api/chirps", ch.GetChirps)
	r.HandleFunc("GET /api/chirps/{id}", ch.GetChirp)

}
