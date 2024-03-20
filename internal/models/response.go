package models

type SignInResponse struct {
	ID            int    `json:"id"`
	Email         string `json:"email"`
	PremiumMember bool   `json:"is_chirpy_red"`
	Token         string `json:"token"`
	RefreshToken  string `json:"refresh_token"`
}

type SignInRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type SignUpRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type SignUpResponse struct {
	Email         string `json:"email"`
	ID            int    `json:"id"`
	PremiumMember bool   `json:"is_chirpy_red"`
}

type UpdateUserRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type UpdateUserResponse struct {
	Email         string `json:"email"`
	ID            int    `json:"id"`
	PremiumMember bool   `json:"is_chirpy_red"`
}

type RefreshTokenResponse struct {
	AccessToken string `json:"token"`
}
