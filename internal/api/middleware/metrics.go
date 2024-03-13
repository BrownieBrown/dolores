package middleware

import (
	"fmt"
	"net/http"
	"sync"
)

type ApiConfig struct {
	fileServerHits int
	mux            sync.Mutex
}

func NewAPIConfig() *ApiConfig {
	return &ApiConfig{}
}

func (cfg *ApiConfig) IncrementFileServerHits(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		cfg.mux.Lock()
		cfg.fileServerHits++
		cfg.mux.Unlock()

		next.ServeHTTP(w, r)
	})
}

func (cfg *ApiConfig) GetFileServerHits(w http.ResponseWriter, r *http.Request) {
	cfg.mux.Lock()
	hits := cfg.fileServerHits
	cfg.mux.Unlock()

	w.Header().Add("Content-Type", "text/html; charset=utf-8")
	w.WriteHeader(http.StatusOK)
	// Format the HTML content with the hits count
	htmlContent := fmt.Sprintf(`
<html>
<body>
    <h1>Welcome, Chirpy Admin</h1>
    <p>Chirpy has been visited %d times!</p>
</body>
</html>
`, hits)
	// Write the formatted HTML content to the response
	w.Write([]byte(htmlContent))
}

func (cfg *ApiConfig) ResetFileServerHits(w http.ResponseWriter, r *http.Request) {
	cfg.mux.Lock()
	cfg.fileServerHits = 0
	cfg.mux.Unlock()

	w.WriteHeader(http.StatusOK)
}
