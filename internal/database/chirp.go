package database

import (
	"errors"
	"github.com/BrownieBrown/dolores/internal/models"
	"strconv"
)

func (db *DB) CreateChirp(body string) (models.Chirp, error) {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return models.Chirp{}, err
	}

	lastChirpID := len(dbContent.Chirps)
	newChirp := models.Chirp{ID: lastChirpID + 1, Body: body}
	dbContent.Chirps[newChirp.ID] = newChirp

	if err = db.writeDB(dbContent); err != nil {
		return models.Chirp{}, err

	}

	return newChirp, nil
}

func (db *DB) GetChirps() ([]models.Chirp, error) {
	db.mux.RLock()
	defer db.mux.RUnlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return []models.Chirp{}, err
	}

	chirps := make([]models.Chirp, 0, len(dbContent.Chirps))
	for _, chirp := range dbContent.Chirps {
		chirps = append(chirps, chirp)
	}

	return chirps, nil
}
func (db *DB) GetChirp(id string) (models.Chirp, error) {
	db.mux.RLock()
	defer db.mux.RUnlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return models.Chirp{}, err
	}

	intID, err := strconv.Atoi(id)
	if err != nil {
		return models.Chirp{}, err

	}

	for _, chirp := range dbContent.Chirps {
		if chirp.ID == intID {
			return chirp, nil
		}
	}

	return models.Chirp{}, errors.New("Chirp not found")
}
