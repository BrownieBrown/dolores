package router

import (
	"github.com/BrownieBrown/dolores/internal/api/handler"
	"net/http"
)

type Router struct {
	*http.ServeMux
}

func NewRouter() *Router {
	return &Router{http.NewServeMux()}
}

func (r *Router) Init(ch *handler.ChirpHandler, hh *handler.HealthHandler, uh *handler.UserHandler, mh *handler.MetricsHandler) {
	fileServerHandler := http.StripPrefix("/app/", http.FileServer(http.Dir(".")))
	r.Handle("/app/", mh.IncrementFileServerHits(fileServerHandler))

	assetHandler := http.StripPrefix("/app/assets/", http.FileServer(http.Dir("./assets")))
	r.Handle("/app/assets/", mh.IncrementFileServerHits(assetHandler))

	r.HandleFunc("GET /api/healthz", hh.GetHealth)

	r.HandleFunc("GET /admin/metrics", mh.GetFileServerHits)

	r.HandleFunc("GET /api/reset", mh.ResetFileServerHits)

	r.HandleFunc("POST /api/chirps", ch.CreateChirp)
	r.HandleFunc("GET /api/chirps", ch.GetChirps)
	r.HandleFunc("GET /api/chirps/{id}", ch.GetChirp)
	r.HandleFunc("DELETE /api/chirps/{id}", ch.DeleteChirp)

	r.HandleFunc("POST /api/users", uh.SignUp)
	r.HandleFunc("POST /api/login", uh.SignIn)
	r.HandleFunc("PUT /api/users", uh.UpdateUser)

	r.HandleFunc("POST /api/refresh", uh.RefreshToken)
	r.HandleFunc("POST /api/revoke", uh.InvalidateRefreshToken)

	r.HandleFunc("POST /api/polka/webhooks", uh.UpdatePremiumMembership)
}
