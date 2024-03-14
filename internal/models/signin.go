package models

type SignInRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type SignInResponse struct {
	ID    int    `json:"id"`
	Email string `json:"email"`
}
