# Chirpy - A Microblogging Platform

Chirpy is a lightweight microblogging platform, enabling users to share short messages, or "chirps", within a community. Inspired by the simplicity of tweeting, Chirpy aims to offer a streamlined, focused platform for quick thoughts and updates.

## Getting Started

Follow these instructions to get Chirpy up and running on your local machine for development and testing. Check out the deployment section for notes on deploying Chirpy in a live environment.

### Prerequisites

Before you begin, ensure you have Go installed on your system. Chirpy is built with Go and uses its standard library heavily, along with a few additional tools:

- Go (1.15 or later recommended)

```bash
# Check Go version
go version
```

### Installing
# Clone the repository
```bash
git clone https://github.com/BrownieBrown/dolores
```

# Navigate into the project directory
```bash
cd dolores
```

# Build the project (optional)
```bash
go build
```

# Run the project
```bash
go run .
```

This starts the chirpy server on the default port. Access it at http://localhost:8080.


### Built With

- [Go](https://golang.org/) - The programming language used
- Custom JWT and bcrypt for authentication and secure password handling

### Authors

- Marco Braun - [BrownieBrown]

### License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details