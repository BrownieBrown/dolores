package database

import (
	"encoding/json"
	"errors"
	"github.com/BrownieBrown/dolores/internal/models"
	"os"
	"strconv"
	"sync"
)

type DB struct {
	path string
	mux  *sync.RWMutex
}

type DBStructure struct {
	Chirps map[int]models.Chirp `json:"chirps"`
}

func NewDB(path string) *DB {
	return &DB{path: path, mux: &sync.RWMutex{}}
}

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

func (db *DB) loadDB() (DBStructure, error) {
	dbContent := DBStructure{Chirps: make(map[int]models.Chirp)}

	data, err := os.ReadFile(db.path)
	if err == nil {
		if err := json.Unmarshal(data, &dbContent); err != nil {
			return DBStructure{}, err
		}
	}

	return dbContent, nil
}

func (db *DB) writeDB(dbContent DBStructure) error {
	data, err := json.Marshal(dbContent)
	if err != nil {
		return err
	}

	if err := os.WriteFile(db.path, data, 0644); err != nil {
		return err
	}

	return nil
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
