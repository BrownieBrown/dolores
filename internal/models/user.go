package models

type User struct {
	ID            int    `json:"id"`
	Email         string `json:"email"`
	Password      []byte `json:"password"`
	PremiumMember bool   `json:"is_chirpy_red"`
}
