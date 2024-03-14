package models

type SignUpResponse struct {
	Email string `json:"email"`
	ID    int    `json:"id"`
}

type SignUpRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}
