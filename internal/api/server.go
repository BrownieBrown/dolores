package api

import (
	"net/http"
)

type Server struct {
	server *http.Server
}

func NewServer(port string, handler http.Handler) *http.Server {
	return &http.Server{
		Addr:    ":" + port,
		Handler: handler,
	}
}

func (s *Server) ListenAndServe() error {
	return s.server.ListenAndServe()
}
