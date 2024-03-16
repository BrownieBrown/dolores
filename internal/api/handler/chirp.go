package handler

import (
	"encoding/json"
	"errors"
	"github.com/BrownieBrown/dolores/internal/config"
	"github.com/BrownieBrown/dolores/internal/database"
	"github.com/BrownieBrown/dolores/internal/models"
	"github.com/BrownieBrown/dolores/internal/utils"
	"net/http"
	"regexp"
	"strconv"
	"strings"
)

type ChirpHandler struct {
	Config   *config.ApiConfig
	Database *database.DB
}

func NewChirpHandler(config *config.ApiConfig, database *database.DB) *ChirpHandler {
	return &ChirpHandler{
		Config:   config,
		Database: database,
	}
}

func (ch *ChirpHandler) CreateChirp(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	tokenString, err := utils.ExtractTokenFromAuthHeader(r)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return
	}

	claims, err := utils.ValidateAccessToken(tokenString, ch.Config)
	if err != nil {
		utils.WriteError(w, http.StatusUnauthorized, err.Error())
		return

	}

	userID, err := strconv.Atoi(claims.Subject)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
		return
	}

	var chirp models.Chirp
	if err := json.NewDecoder(r.Body).Decode(&chirp); err != nil {
		utils.WriteError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	if err := validateChirp(chirp); err != nil {
		utils.WriteError(w, http.StatusBadRequest, err.Error())
		return // Make sure to return after writing the error
	}

	result, err := cleanUpMessage(chirp.Body)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
		return
	}

	chirp.Body = result
	chirp.AuthorID = userID

	newChirp, err := ch.Database.CreateChirp(chirp)

	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Failed to create chirp")
		return
	}

	utils.WriteData(w, http.StatusCreated, newChirp)
}

func (ch *ChirpHandler) GetChirps(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
	}

	chirps, err := ch.Database.GetChirps()
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
		return
	}

	utils.WriteData(w, http.StatusOK, chirps)
}

func (ch *ChirpHandler) GetChirp(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		utils.WriteError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	id := r.PathValue("id")
	if id == "" {
		utils.WriteError(w, http.StatusBadRequest, "Missing id parameter")
		return
	}

	chirp, err := ch.Database.GetChirp(id)
	if err != nil {
		utils.WriteError(w, http.StatusNotFound, "Chirp not found")
		return
	}

	utils.WriteData(w, http.StatusOK, chirp)
}

func validateChirp(chirp models.Chirp) error {
	maxLength := 140
	minLength := 1
	messageLength := len(chirp.Body)

	return validateChirpLength(messageLength, minLength, maxLength)
}

func validateChirpLength(messageLength, minLength, maxLength int) error {
	if messageLength < minLength {
		return errors.New("Chirp is too short")
	}

	if messageLength > maxLength {
		return errors.New("Chirp is too long")
	}

	return nil
}

func cleanUpMessage(input string) (string, error) {
	forbiddenWords := []string{"sharbert", "kerfuffle", "fornax"}
	replacementWord := "****"

	for i, word := range forbiddenWords {
		forbiddenWords[i] = regexp.QuoteMeta(word)
	}

	pattern := "(?i)\\b(" + strings.Join(forbiddenWords, "|") + ")\\b"
	re, err := regexp.Compile(pattern)
	if err != nil {
		return "", err // Return error if the regular expression fails to compile.
	}

	result := re.ReplaceAllString(input, replacementWord)

	return result, nil
}
