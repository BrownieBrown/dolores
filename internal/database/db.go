package database

import (
	"encoding/json"
	"github.com/BrownieBrown/dolores/internal/models"
	"os"
	"sync"
)

type DB struct {
	path string
	mux  *sync.RWMutex
}

type DBStructure struct {
	Chirps map[int]models.Chirp `json:"chirps"`
	Users  map[int]models.User  `json:"users"`
}

func NewDB(path string) *DB {
	return &DB{path: path, mux: &sync.RWMutex{}}
}

func (db *DB) loadDB() (DBStructure, error) {
	dbContent := DBStructure{Chirps: make(map[int]models.Chirp), Users: make(map[int]models.User)}

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
