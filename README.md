# 🎯 CareerRoadmap — AI-Powered Career Recommendation & Roadmap System

A full-stack career roadmap and resume parsing platform combining a **Spring Boot backend** and a **Python FastAPI AI service**. Students can upload resumes, get AI-powered skill extraction, receive career recommendations, follow personalized roadmaps, and take skill assessments.

---

## 🏗️ Architecture

```
┌────────────────────┐      ┌────────────────────┐      ┌──────────────┐
│   Frontend Client  │─────▶│  Spring Boot API   │─────▶│  PostgreSQL  │
│   (any HTTP client)│      │  Port 10000        │      │  Port 5432   │
└────────────────────┘      └────────┬───────────┘      └──────────────┘
                                     │
                                     │ HTTP
                                     ▼
                            ┌────────────────────┐
                            │  FastAPI AI Service │
                            │  Port 8000         │
                            └────────────────────┘
```

| Component | Technology | Port | Description |
|-----------|------------|------|-------------|
| **Backend** | Spring Boot 4.1, Spring Security, Spring Data JPA, Hibernate, JWT (jjwt), Lombok, Cloudinary | `10000` | REST API, Authentication, Database CRUD, AI Integration |
| **AI Service** | FastAPI, PyMuPDF (fitz), Pydantic, Uvicorn | `8000` | Resume PDF text extraction, Skill matching & analysis |
| **Database** | PostgreSQL | `5432` | Relational storage (Database: `Student`) |

---

## 📋 Table of Contents

- [Architecture](#-architecture)
- [API Reference](#-api-reference)
  - [Authentication](#1--authentication--user-management)
  - [Student Profile](#2--student-profile)
  - [Student Skills](#3--student-skills)
  - [Resume Management](#4--resume-management)
  - [Resume Analysis](#5--resume-analysis)
  - [Career Management](#6--career-management)
  - [Career Recommendations](#7--career-recommendations)
  - [Roadmaps](#8--roadmaps)
  - [Assessments](#9--assessments)
  - [Learning Resources](#10--learning-resources)
  - [Progress Tracking](#11--progress-tracking)
  - [Admin Dashboard](#12--admin-dashboard)
  - [AI Service (Python)](#13--ai-service-python-fastapi)
- [Configuration](#%EF%B8%8F-configuration)
- [How to Run](#-how-to-run)

---

## 📡 API Reference

**Base URL:** `http://localhost:10000`

> All endpoints (except Auth register/login/logout) require a valid JWT cookie (`career_jwt`) or `Authorization: Bearer <token>` header.

### Authentication Legend

| Icon | Meaning |
|------|---------|
| 🔓 | Public — no authentication required |
| 🔒 | Authenticated — any logged-in user |
| 🛡️ | Admin only — requires `ROLE_ADMIN` |

---

### 1. 🔐 Authentication & User Management

**Base Path:** `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/register` | 🔓 | Register a new student |
| `POST` | `/api/auth/login` | 🔓 | Login and receive JWT (set as HttpOnly cookie) |
| `POST` | `/api/auth/register/admin` | 🔓 | Register an admin user |
| `POST` | `/api/auth/logout` | 🔒 | Logout (clears JWT cookie) |
| `GET` | `/api/auth/me` | 🔒 | Get current authenticated user info |

#### `POST /api/auth/register`

```json
// Request Body
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

// Response — 201 Created
{
  "message": "User registered successfully",
  "email": "john@example.com"
}
```

#### `POST /api/auth/login`

```json
// Request Body
{
  "email": "john@example.com",
  "password": "securePassword123"
}

// Response — 200 OK (also sets HttpOnly cookie: career_jwt)
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "email": "john@example.com",
  "name": "John Doe"
}
```

#### `POST /api/auth/logout`

```json
// Response — 200 OK (clears JWT cookie)
{
  "message": "Logged out successfully"
}
```

#### `GET /api/auth/me`

```json
// Response — 200 OK
{
  "email": "john@example.com",
  "authorities": ["ROLE_USER"]
}
```

---

### 2. 👤 Student Profile

**Base Path:** `/api/profile`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/profile/me` | 🔒 | Create student profile |
| `GET` | `/api/profile/me` | 🔒 | Get logged-in student's profile |
| `PUT` | `/api/profile/me` | 🔒 | Update logged-in student's profile |

#### `POST /api/profile/me`

```json
// Request Body
{
  "fullName": "John Doe",
  "university": "MIT",
  "degree": "B.Tech Computer Science",
  "graduationYear": 2025,
  "bio": "Aspiring software engineer"
}

// Response — 201 Created
{
  "id": 1,
  "fullName": "John Doe",
  "university": "MIT",
  "degree": "B.Tech Computer Science",
  "graduationYear": 2025,
  "bio": "Aspiring software engineer"
}
```

---

### 3. 🛠️ Student Skills

**Base Path:** `/api/skills`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/skills/me` | 🔒 | Get all skills of logged-in student |
| `POST` | `/api/skills/me` | 🔒 | Manually add a skill |
| `PUT` | `/api/skills/me/{id}` | 🔒 | Update skill proficiency/experience |
| `DELETE` | `/api/skills/me/{id}` | 🔒 | Delete a skill |

#### `POST /api/skills/me`

```json
// Request Body
{
  "skillName": "Java",
  "proficiencyLevel": "INTERMEDIATE",
  "yearsOfExperience": 2
}

// Response — 201 Created
{
  "id": 1,
  "skillName": "Java",
  "proficiencyLevel": "INTERMEDIATE",
  "yearsOfExperience": 2
}
```

#### `PUT /api/skills/me/{id}`

```json
// Request Body
{
  "proficiencyLevel": "ADVANCED",
  "yearsOfExperience": 3
}
```

---

### 4. 📄 Resume Management

**Base Path:** `/api/resumes`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/resumes/upload` | 🔒 | Upload a PDF resume (max 5 MB) |
| `GET` | `/api/resumes` | 🔒 | List all resumes of logged-in user |
| `GET` | `/api/resumes/download?fileUrl=<url>` | 🔒 | Download a resume from Cloudinary |

#### `POST /api/resumes/upload`

```
Content-Type: multipart/form-data

file: <resume.pdf>
```

```json
// Response — 200 OK
{
  "id": 1,
  "originalFileName": "john_resume.pdf",
  "contentType": "application/pdf",
  "fileSize": 245760,
  "uploadedAt": "2025-01-15T10:30:00",
  "storedFileName": "abc123-resume.pdf"
}
```

> **Validation:** Only `.pdf` files allowed, max 5 MB, PDF signature (`%PDF-`) verified.

#### `GET /api/resumes/download?fileUrl=<url>`

Downloads the resume file. Only HTTPS URLs from `res.cloudinary.com` are accepted.

```
Response: application/pdf binary
Content-Disposition: attachment; filename="resume.pdf"
```

---

### 5. 🤖 Resume Analysis

**Base Path:** `/api/resumes`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/resumes/{id}/analyze` | 🔒 | Analyze a resume using the AI service |

#### `POST /api/resumes/{id}/analyze`

Sends the uploaded resume to the Python AI service for text extraction and skill analysis. Extracted skills are automatically saved to the student's skill profile.

```json
// Response — 200 OK
{
  "filename": "john_resume.pdf",
  "total_pages": 2,
  "extracted_text": "--- Page 1 ---\nJohn Doe\nSoftware Engineer...",
  "skills_analysis": {
    "total_skills_found": 8,
    "all_skills": ["Java", "Spring Boot", "Python", "React", "Docker", "Git", "PostgreSQL", "AWS"],
    "categorized_skills": {
      "Programming Languages": ["Java", "Python"],
      "Backend": ["Spring Boot"],
      "Frontend": ["React"],
      "Databases": ["PostgreSQL"],
      "Tools": ["Docker", "Git"],
      "Cloud": ["AWS"]
    }
  }
}
```

---

<<<<<<< HEAD
### 6. 💼 Career Management
=======
### 🔄 6. Update Latest Resume

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `PUT` | `/api/resumes/latest` | 🔒 | Update the latest uploaded resume of the logged-in user |

#### `PUT /api/resumes/latest/update`

Replaces the latest uploaded resume of the authenticated user with a new PDF file.

**Request:**

```http
PUT /api/resumes/latest
Content-Type: multipart/form-data
```

**Request Body (form-data):**

| Key | Type | Value |
|-----|------|-------|
| `file` | File | `new_resume.pdf` |

**Response — 200 OK**

```json
{
  "id": 1,
  "originalFileName": "new_resume.pdf",
  "contentType": "application/pdf",
  "fileSize": 125000,
  "storedFileName": "https://res.cloudinary.com/..."
}
```

**Functionality:**
- Fetches the latest resume uploaded by the logged-in user.
- Uploads the new PDF to Cloudinary.
- Updates the existing resume record with the new Cloudinary URL.
- Updates the original filename, content type and file size.
- Does not create a duplicate resume database record.

**Validation:**
- Only PDF files are accepted.
- Maximum file size: 5 MB.
- PDF signature (`%PDF-`) is verified.
- User authentication is required.

**Possible Error Responses:**

| Status | Description |
|--------|-------------|
| `400 Bad Request` | Invalid file or file exceeds 5 MB |
| `401 Unauthorized` | User is not authenticated |
| `404 Not Found` | No existing resume found for the user |

### 7. 💼 Career Management
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/careers`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/careers` | 🛡️ | Create a new career path |
| `GET` | `/api/careers` | 🔒 | List all careers |
| `GET` | `/api/careers/{id}` | 🔒 | Get a career by ID |
| `POST` | `/api/careers/{careerId}/skills` | 🛡️ | Add a required skill to a career |
| `DELETE` | `/api/careers/{careerId}/skills/{skillId}` | 🛡️ | Remove a skill from a career |

#### `POST /api/careers`

```json
// Request Body
{
  "name": "Backend Developer",
  "description": "Develops server-side logic and APIs"
}

// Response — 201 Created
{
  "id": 1,
  "name": "Backend Developer",
  "description": "Develops server-side logic and APIs",
  "skills": []
}
```

#### `POST /api/careers/{careerId}/skills`

```json
// Request Body
{
  "skillName": "Java",
  "importance": "ESSENTIAL"
}

// Response — 201 Created
{
  "id": 1,
  "skillName": "Java",
  "importance": "ESSENTIAL"
}
```

---

<<<<<<< HEAD
### 7. 🎯 Career Recommendations
=======
### 8. 🎯 Career Recommendations
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/recommendations`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/recommendations/careers` | 🔒 | Get career recommendations based on student skills |
| `GET` | `/api/recommendations/careers/{careerId}/skill-gaps` | 🔒 | Get skill gap analysis for a specific career |

#### `GET /api/recommendations/careers`

Returns a ranked list of career paths matching the student's current skills.

```json
// Response — 200 OK
[
  {
    "careerId": 1,
    "careerName": "Backend Developer",
    "matchPercentage": 75.0,
    "matchedSkills": ["Java", "Spring Boot", "PostgreSQL"],
    "missingSkills": ["Kubernetes"]
  }
]
```

#### `GET /api/recommendations/careers/{careerId}/skill-gaps`

```json
// Response — 200 OK
{
  "careerId": 1,
  "careerName": "Backend Developer",
  "matchPercentage": 75.0,
  "matchedSkills": ["Java", "Spring Boot"],
  "missingSkills": ["Kubernetes", "Redis"]
}
```

---

<<<<<<< HEAD
### 8. 🗺️ Roadmaps
=======
### 9. 🗺️ Roadmaps
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/roadmaps`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/roadmaps` | 🔒 | Create a personalized roadmap |
| `GET` | `/api/roadmaps` | 🔒 | List current user's roadmaps |
| `GET` | `/api/roadmaps/{id}` | 🔒 | Get a specific roadmap with items |
| `PATCH` | `/api/roadmaps/{roadmapId}/items/{itemId}/status` | 🔒 | Update a roadmap item's completion status |

#### `POST /api/roadmaps`

```json
// Request Body
{
  "careerId": 1,
  "title": "My Backend Developer Roadmap"
}

// Response — 201 Created
{
  "id": 1,
  "title": "My Backend Developer Roadmap",
  "careerName": "Backend Developer",
  "items": [
    {
      "id": 1,
      "skillName": "Kubernetes",
      "status": "NOT_STARTED",
      "orderIndex": 1
    }
  ]
}
```

#### `PATCH /api/roadmaps/{roadmapId}/items/{itemId}/status`

```json
// Request Body
{
  "status": "IN_PROGRESS"
}

// Response — 200 OK
{
  "id": 1,
  "skillName": "Kubernetes",
  "status": "IN_PROGRESS",
  "orderIndex": 1
}
```

---

<<<<<<< HEAD
### 9. 📝 Assessments
=======
### 10. 📝 Assessments
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/assessments`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/assessments` | 🛡️ | Create an assessment |
| `GET` | `/api/assessments` | 🔒 | List all active assessments |
| `POST` | `/api/assessments/{assessmentId}/questions` | 🛡️ | Add a question to an assessment |
| `GET` | `/api/assessments/{assessmentId}/questions` | 🔒 | Get questions (without correct answers) |
| `POST` | `/api/assessments/{assessmentId}/attempts` | 🔒 | Start an assessment attempt |
| `GET` | `/api/assessments/my-attempts` | 🔒 | Get all of the student's attempts |
| `POST` | `/api/assessments/attempts/{attemptId}/submit` | 🔒 | Submit answers and get result |
| `GET` | `/api/assessments/attempts/{attemptId}/result` | 🔒 | Get detailed result of an attempt |

#### `POST /api/assessments` (Admin)

```json
// Request Body
{
  "title": "Java Fundamentals",
  "description": "Test your core Java knowledge",
  "skillName": "Java",
  "durationMinutes": 30
}

// Response — 201 Created
{
  "id": 1,
  "title": "Java Fundamentals",
  "description": "Test your core Java knowledge",
  "skillName": "Java",
  "durationMinutes": 30
}
```

#### `POST /api/assessments/{assessmentId}/questions` (Admin)

```json
// Request Body
{
  "questionText": "What is the default value of an int in Java?",
  "options": ["null", "0", "undefined", "1"],
  "correctOptionIndex": 1
}
```

#### `POST /api/assessments/attempts/{attemptId}/submit`

```json
// Request Body
{
  "answers": [
    { "questionId": 1, "selectedOptionIndex": 1 },
    { "questionId": 2, "selectedOptionIndex": 0 }
  ]
}

// Response — 200 OK
{
  "attemptId": 1,
  "totalQuestions": 10,
  "correctAnswers": 8,
  "scorePercentage": 80.0,
  "passed": true,
  "answers": [
    {
      "questionId": 1,
      "selectedOptionIndex": 1,
      "correctOptionIndex": 1,
      "correct": true
    }
  ]
}
```

---

<<<<<<< HEAD
### 10. 📚 Learning Resources
=======
### 11. 📚 Learning Resources
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/resources`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/resources` | 🛡️ | Create a learning resource |
| `GET` | `/api/resources` | 🔒 | List all learning resources |
| `GET` | `/api/resources/skill/{skillId}?freeOnly=false` | 🔒 | Get resources for a specific skill |

#### `POST /api/resources` (Admin)

```json
// Request Body
{
  "title": "Spring Boot Tutorial",
  "url": "https://example.com/spring-boot",
  "type": "VIDEO",
  "skillId": 1,
  "free": true
}

// Response — 201 Created
{
  "id": 1,
  "title": "Spring Boot Tutorial",
  "url": "https://example.com/spring-boot",
  "type": "VIDEO",
  "skillName": "Spring Boot",
  "free": true
}
```

#### `GET /api/resources/skill/{skillId}?freeOnly=true`

Returns only free resources when `freeOnly=true`.

---

<<<<<<< HEAD
### 11. 📊 Progress Tracking
=======
### 12. 📊 Progress Tracking
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/progress`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/progress/summary` | 🔒 | Get overall progress across all roadmaps |
| `GET` | `/api/progress/roadmaps/{roadmapId}` | 🔒 | Get progress for a specific roadmap |

#### `GET /api/progress/summary`

```json
// Response — 200 OK
{
  "totalItems": 15,
  "completedItems": 8,
  "inProgressItems": 3,
  "notStartedItems": 4,
  "completionPercentage": 53.3
}
```

---

<<<<<<< HEAD
### 12. 🛡️ Admin Dashboard
=======
### 13. 🛡️ Admin Dashboard
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base Path:** `/api/admin`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/admin/dashboard` | 🛡️ | Get platform-wide statistics |

#### `GET /api/admin/dashboard`

```json
// Response — 200 OK
{
  "totalUsers": 150,
  "totalResumes": 230,
  "totalCareers": 12,
  "totalAssessments": 25,
  "totalAttempts": 480
}
```

---

<<<<<<< HEAD
### 13. 🐍 AI Service (Python FastAPI)
=======
### 14. 🐍 AI Service (Python FastAPI)
>>>>>>> 68bc9fe (Add latest resume update feature)

**Base URL:** `http://localhost:8000`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Service home / status message |
| `GET` | `/health` | Health check |
| `POST` | `/parse-resume` | Extract text from PDF |
| `POST` | `/extract-skills` | Extract skills from text |
| `POST` | `/analyze-resume` | Parse PDF + extract skills (combined) |

#### `GET /health`

```json
{
  "status": "UP",
  "service": "AI Resume Parser"
}
```

#### `POST /parse-resume`

```
Content-Type: multipart/form-data
file: <resume.pdf>
```

```json
// Response — 200 OK
{
  "filename": "resume.pdf",
  "total_pages": 2,
  "extracted_text": "--- Page 1 ---\nJohn Doe..."
}
```

#### `POST /extract-skills`

```json
// Request Body
{
  "extracted_text": "Experienced Java developer with Spring Boot, Docker, AWS..."
}

// Response — 200 OK
{
  "total_skills_found": 4,
  "all_skills": ["Java", "Spring Boot", "Docker", "AWS"],
  "categorized_skills": {
    "Programming Languages": ["Java"],
    "Backend": ["Spring Boot"],
    "Tools": ["Docker"],
    "Cloud": ["AWS"]
  }
}
```

**Supported Skill Categories:**

| Category | Skills |
|----------|--------|
| Programming Languages | Java, Python, C++, C#, JavaScript, TypeScript, PHP, Ruby, Go, Kotlin, Swift |
| Backend | Spring Boot, Spring Framework, Node.js, Express.js, Django, Flask, FastAPI, REST API, Hibernate, JDBC |
| Frontend | HTML, CSS, React, Angular, Vue.js, Bootstrap, Tailwind CSS |
| Databases | MySQL, PostgreSQL, MongoDB, Oracle, Redis, SQLite |
| Tools | Git, GitHub, Docker, Kubernetes, Jenkins, Maven, Postman, Linux |
| Machine Learning | Machine Learning, Deep Learning, TensorFlow, PyTorch, Scikit-learn, Pandas, NumPy, NLP |
| Cloud | AWS, Azure, Google Cloud, Firebase |

#### `POST /analyze-resume`

Combined endpoint — parses PDF and extracts skills in one call. Same file upload as `/parse-resume`, returns both extracted text and skills analysis.

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USER` | PostgreSQL username | `root` |
| `DB_PASSWORD` | PostgreSQL password | *(required)* |
| `JWT_SECRET` | 256-bit Base64 secret for signing tokens | `HbfIjU2f/1l8BV...` |
| `CLOUD_NAME` | Cloudinary cloud name | *(required)* |
| `CLOUD_API` | Cloudinary API key | *(required)* |
| `CLOUD_SECRET_KEY` | Cloudinary API secret | *(required)* |

### Database

- **Driver:** PostgreSQL
- **URL:** `jdbc:postgresql://localhost:5432/Student`
- **DDL Mode:** `update` (auto-creates/updates tables)

### JWT Configuration

- **Cookie name:** `career_jwt`
- **Expiration:** 24 hours (86400000 ms)
- **Cookie secure:** `false` (set to `true` in production)
- **SameSite:** `Lax`

---

## 🚀 How to Run

### Prerequisites

- **Java 21**
- **Python 3.10+**
- **PostgreSQL** with a database named `Student`
- **Cloudinary** account (for resume file storage)

### Option 1: Run All Services Together

```cmd
.\start-all.bat
```

### Option 2: Run Services Individually

#### 1. AI Service (Python FastAPI)

```cmd
.\start-ai-service.bat
```

Or manually:

```cmd
cd ai-service
.\.venv\Scripts\activate
uvicorn main:app --host 127.0.0.1 --port 8000 --reload
```

#### 2. Backend (Spring Boot)

```cmd
.\start-backend.bat
```

Or manually:

```cmd
cd backend\careerroadmap
.\mvnw.cmd spring-boot:run
```

### Verification

| Check | URL |
|-------|-----|
| AI Service Health | `http://localhost:8000/health` |
| AI Swagger UI | `http://localhost:8000/docs` |
| Backend Running | `http://localhost:10000/api/auth/me` (returns 401 = running) |

---

## 🔒 Security Summary

| Endpoint Pattern | Access |
|------------------|--------|
| `POST /api/auth/register`, `/login`, `/logout` | Public |
| `POST /api/careers`, `POST /api/careers/*/skills`, `DELETE /api/careers/*/skills/*` | Admin only |
| `POST /api/assessments`, `POST /api/assessments/*/questions` | Admin only |
| `POST /api/resources` | Admin only |
| `/api/admin/**` | Admin only |
| All other endpoints | Authenticated users |

---

## 🧪 Testing

```cmd
cd backend\careerroadmap
.\mvnw.cmd test
```

---

## 📁 Project Structure

```
CareerRoadmap/
├── backend/
│   └── careerroadmap/
│       ├── src/main/java/com/utkarsh/careerroadmap/
│       │   ├── controller/        # REST API controllers
│       │   ├── dto/               # Request/Response DTOs
│       │   ├── entity/            # JPA entities
│       │   ├── exception/         # Custom exceptions
│       │   ├── repository/        # Spring Data JPA repositories
│       │   ├── security/          # JWT + Spring Security config
│       │   └── service/           # Business logic
│       ├── src/main/resources/
│       │   └── application.properties
│       └── pom.xml
├── ai-service/
│   ├── main.py                    # FastAPI endpoints
│   ├── skill_extractor.py         # Skill matching logic
│   └── .venv/                     # Python virtual environment
├── .gitignore
└── README.md
```

---

## 📜 License

This project is developed as part of a **5th Semester Software Engineering** course.
