package main

import (
	"github.com/BrownieBrown/dolores/internal/api/handler"
	middleware2 "github.com/BrownieBrown/dolores/internal/api/middleware"
	"github.com/BrownieBrown/dolores/internal/api/router"
	"github.com/BrownieBrown/dolores/internal/api/server"
	"github.com/BrownieBrown/dolores/internal/config"
	"github.com/BrownieBrown/dolores/internal/database"
	"github.com/joho/godotenv"
	"log"
)

func main() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env file")
	}

	const dbPath = "./database.json"
	const port = "8080"

	cfg := config.LoadConfig()

	r := router.NewRouter()
	db := database.NewDB(dbPath)
	ch := handler.NewChirpHandler(cfg, db)
	hh := handler.NewHealthHandler(cfg)
	uh := handler.NewUserHandler(cfg, db)
	mh := handler.NewMetricsHandler(cfg)
	r.Init(ch, hh, uh, mh)

	corsMux := middleware2.Cors(r)

	srv := server.NewServer(port, corsMux)
	err = srv.ListenAndServe()
	if err != nil {
		return
	}
}
