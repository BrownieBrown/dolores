package handler

import (
	"fmt"
	"github.com/BrownieBrown/dolores/internal/config"
	"net/http"
	"sync"
)

type MetricsHandler struct {
	fileServerHits int
	mux            sync.Mutex
}

func NewMetricsHandler(cfg *config.ApiConfig) *MetricsHandler {
	return &MetricsHandler{}
}

func (mh *MetricsHandler) IncrementFileServerHits(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		mh.mux.Lock()
		mh.fileServerHits++
		mh.mux.Unlock()

		next.ServeHTTP(w, r)
	})
}

func (mh *MetricsHandler) GetFileServerHits(w http.ResponseWriter, r *http.Request) {
	mh.mux.Lock()
	hits := mh.fileServerHits
	mh.mux.Unlock()

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

func (mh *MetricsHandler) ResetFileServerHits(w http.ResponseWriter, r *http.Request) {
	mh.mux.Lock()
	mh.fileServerHits = 0
	mh.mux.Unlock()

	w.WriteHeader(http.StatusOK)
}
