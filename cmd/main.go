package main

import (
	"github.com/BrownieBrown/dolores/internal/api"
	"github.com/BrownieBrown/dolores/internal/api/handler"
	middleware2 "github.com/BrownieBrown/dolores/internal/api/middleware"
	"github.com/BrownieBrown/dolores/internal/api/router"
	"github.com/BrownieBrown/dolores/internal/database"
)

func main() {
	const dbPath = "./database.json"
	const port = "8080"
	apiCfg := middleware2.NewAPIConfig()
	r := router.NewRouter()
	db := database.NewDB(dbPath)
	ch := handler.NewChirpHandler(db)
	hh := handler.NewHealthHandler()
	uh := handler.NewUserHandler(db)
	r.Init(apiCfg, ch, hh, uh)

	corsMux := middleware2.Cors(r)

	srv := api.NewServer(port, corsMux)
	err := srv.ListenAndServe()
	if err != nil {
		return
	}
}
