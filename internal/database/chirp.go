package database

import (
	"errors"
	"github.com/BrownieBrown/dolores/internal/models"
	"sort"
	"strconv"
)

func (db *DB) CreateChirp(chirp models.Chirp) (models.Chirp, error) {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return models.Chirp{}, err
	}

	lastChirpID := len(dbContent.Chirps)
	newChirp := models.Chirp{ID: lastChirpID + 1, Body: chirp.Body, AuthorID: chirp.AuthorID}
	dbContent.Chirps[newChirp.ID] = newChirp

	if err = db.writeDB(dbContent); err != nil {
		return models.Chirp{}, err

	}

	return newChirp, nil
}

func (db *DB) GetChirps(sortOrder string) ([]models.Chirp, error) {
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

	if sortOrder == "desc" {
		sortDescending(chirps)
		return chirps, nil
	}

	sortAscending(chirps)

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

	return models.Chirp{}, errors.New("chirp not found")
}

func (db *DB) DeleteChirp(id string) error {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return err
	}

	intID, err := strconv.Atoi(id)
	if err != nil {
		return err
	}

	if _, ok := dbContent.Chirps[intID]; !ok {
		return errors.New("chirp not found")
	}

	delete(dbContent.Chirps, intID)

	if err = db.writeDB(dbContent); err != nil {
		return err
	}

	return nil
}

func (db *DB) GetChirpsByAuthorID(id string) ([]models.Chirp, error) {
	db.mux.RLock()
	defer db.mux.RUnlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return []models.Chirp{}, err
	}

	authorID, err := strconv.Atoi(id)
	if err != nil {
		return []models.Chirp{}, err

	}

	chirps := make([]models.Chirp, 0, len(dbContent.Chirps))
	for _, chirp := range dbContent.Chirps {
		if chirp.AuthorID == authorID {
			chirps = append(chirps, chirp)
		}
	}

	return chirps, nil
}

func sortAscending(chirps []models.Chirp) []models.Chirp {
	sort.Slice(chirps, func(i, j int) bool {
		return chirps[i].ID < chirps[j].ID
	})

	return chirps
}

func sortDescending(chirps []models.Chirp) []models.Chirp {
	sort.Slice(chirps, func(i, j int) bool {
		return chirps[i].ID > chirps[j].ID
	})

	return chirps
}
