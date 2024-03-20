package handler

import (
	"encoding/json"
	"errors"
	"github.com/BrownieBrown/dolores/internal/config"
	"github.com/BrownieBrown/dolores/internal/database"
	"github.com/BrownieBrown/dolores/internal/models"
	"github.com/BrownieBrown/dolores/internal/utils"
	"net/http"
	"net/url"
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

	queryParams := r.URL.Query()
	defaultSortOrder := "asc"

	if len(queryParams) > 0 {
		ch.handleQueryParams(queryParams, w)
		return
	}

	chirps, err := ch.Database.GetChirps(defaultSortOrder)

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

func (ch *ChirpHandler) DeleteChirp(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodDelete {
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

	userID, err := strconv.Atoi(claims.Subject)
	if err != nil {
		utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
		return
	}

	if chirp.AuthorID != userID {
		utils.WriteError(w, http.StatusForbidden, "You are not allowed to delete this chirp")
		return
	}

	err = ch.Database.DeleteChirp(id)
	if err != nil {
		utils.WriteError(w, http.StatusNotFound, "Chirp not found")
		return
	}

	utils.WriteData(w, http.StatusOK, nil)
}

func validateChirp(chirp models.Chirp) error {
	maxLength := 140
	minLength := 1
	messageLength := len(chirp.Body)

	return validateChirpLength(messageLength, minLength, maxLength)
}

func validateChirpLength(messageLength, minLength, maxLength int) error {
	if messageLength < minLength {
		return errors.New("chirp is too short")
	}

	if messageLength > maxLength {
		return errors.New("chirp is too long")
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

func (ch *ChirpHandler) handleQueryParams(queryParams url.Values, w http.ResponseWriter) {
	authorID := queryParams.Get("author_id")
	sortOrder := queryParams.Get("sort")

	if authorID != "" {

		chirps, err := ch.Database.GetChirpsByAuthorID(authorID)
		if err != nil {
			utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
			return
		}

		utils.WriteData(w, http.StatusOK, chirps)
		return
	}

	if sortOrder == "desc" {
		chirps, err := ch.Database.GetChirps(sortOrder)
		if err != nil {
			utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
			return
		}

		utils.WriteData(w, http.StatusOK, chirps)
		return
	}

	if sortOrder == "asc" {
		chirps, err := ch.Database.GetChirps(sortOrder)
		if err != nil {
			utils.WriteError(w, http.StatusInternalServerError, "Internal Server Error")
			return
		}

		utils.WriteData(w, http.StatusOK, chirps)
		return
	}
}
