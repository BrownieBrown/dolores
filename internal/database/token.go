package database

import (
	"time"
)

func (db *DB) RefreshTokenIsInvalid(token string) bool {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return false
	}

	for refreshToken := range dbContent.InvalidRefreshTokens {
		if refreshToken == token {
			return true
		}
	}

	return false
}

func (db *DB) InvalidateRefreshToken(token string) error {
	db.mux.Lock()
	defer db.mux.Unlock()

	dbContent, err := db.loadDB()
	if err != nil {
		return err
	}

	if dbContent.InvalidRefreshTokens == nil {
		dbContent.InvalidRefreshTokens = make(map[string]time.Time)
	}

	dbContent.InvalidRefreshTokens[token] = time.Now()

	return db.writeDB(dbContent)
}
