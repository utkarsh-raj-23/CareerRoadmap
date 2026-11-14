# 🎯 CareerRoadmap — Complete API Specification & Request Body Guide

This document contains the exact **Request Body (JSON / Multipart)**, **Query Parameters**, **Path Variables**, and **Headers** required for every single endpoint in the **Spring Boot Backend (Port 10000)** and the **Python FastAPI AI Service (Port 8000)**.

---

## 📋 Table of Contents

- [Authentication & Headers](#-authentication--headers)
- [Part 1: Spring Boot Backend APIs (Port 10000)](#part-1-spring-boot-backend-apis-port-10000)
  - [1. Authentication (`/api/auth`)](#1-authentication-apiauth)
  - [2. Student Profile (`/api/profile`)](#2-student-profile-apiprofile)
  - [3. Student Skills (`/api/skills`)](#3-student-skills-apiskills)
  - [4. Resume Management (`/api/resumes`)](#4-resume-management-apiresumes)
  - [5. AI Resume Review, ATS & Skill Parsing (`/api/resumes`)](#5-ai-resume-review-ats--skill-parsing-apiresumes)
  - [6. Career Management (`/api/careers`)](#6-career-management-apicareers)
  - [7. Career Recommendations & Semantic Gap Analysis (`/api/recommendations`)](#7-career-recommendations--semantic-gap-analysis-apirecommendations)
  - [8. Roadmaps & AI Generation (`/api/roadmaps`)](#8-roadmaps--ai-generation-apiroadmaps)
  - [9. Assessments & Quizzes (`/api/assessments`)](#9-assessments--quizzes-apiassessments)
  - [10. Learning Resources (`/api/resources`)](#10-learning-resources-apiresources)
  - [11. Progress Tracking (`/api/progress`)](#11-progress-tracking-apiprogress)
  - [12. Admin Dashboard (`/api/admin`)](#12-admin-dashboard-apiadmin)
- [Part 2: Python FastAPI AI Service APIs (Port 8000)](#part-2-python-fastapi-ai-service-apis-port-8000)
  - [1. System & Health](#1-system--health)
  - [2. Text Extraction & Skill Parsing](#2-text-extraction--skill-parsing)
  - [3. AI Resume Review & ATS Scoring](#3-ai-resume-review--ats-scoring)
  - [4. Dynamic Roadmap Recommendation](#4-dynamic-roadmap-recommendation)
  - [5. Semantic Skill Analysis](#5-semantic-skill-analysis)

---

## 🔑 Authentication & Headers

- Public endpoints need no headers.
- Authenticated endpoints accept:
  - Header: `Authorization: Bearer <jwt_token>`
  - **OR** Cookie: `career_jwt=<jwt_token>` (automatically set upon login)

---

# Part 1: Spring Boot Backend APIs (Port 10000)

**Base URL:** `http://localhost:10000`

---

### 1. Authentication (`/api/auth`)

#### `POST /api/auth/register`
- **Auth:** 🔓 Public
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "fullName": "Prakhar Kumar",
  "email": "prakhar@example.com",
  "password": "SecurePassword123"
}
```

#### `POST /api/auth/login`
- **Auth:** 🔓 Public
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "email": "prakhar@example.com",
  "password": "SecurePassword123"
}
```

#### `POST /api/auth/register/admin`
- **Auth:** 🔓 Public
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "fullName": "System Admin",
  "email": "admin@example.com",
  "password": "AdminPassword123"
}
```

#### `POST /api/auth/logout`
- **Auth:** 🔒 Authenticated
- **Request Body:** None (Clears `career_jwt` cookie)

#### `GET /api/auth/me`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

---

### 2. Student Profile (`/api/profile`)

#### `POST /api/profile/me`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "fullName": "Prakhar Kumar",
  "university": "National Institute of Technology",
  "degree": "B.Tech Computer Science",
  "graduationYear": 2026,
  "bio": "Aspiring backend developer passionate about microservices and Spring Boot."
}
```

#### `GET /api/profile/me`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `PUT /api/profile/me`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "fullName": "Prakhar Kumar",
  "university": "National Institute of Technology",
  "degree": "B.Tech Computer Science & Engineering",
  "graduationYear": 2026,
  "bio": "Senior student specializing in Java backend development and cloud architecture."
}
```

---

### 3. Student Skills (`/api/skills`)

#### `GET /api/skills/me`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `POST /api/skills/me`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "skillName": "Java",
  "proficiencyLevel": "INTERMEDIATE",
  "yearsOfExperience": 2
}
```
> Note: `proficiencyLevel` can be `BEGINNER`, `INTERMEDIATE`, or `ADVANCED`.

#### `PUT /api/skills/me/{id}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, e.g. `1`)
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "proficiencyLevel": "ADVANCED",
  "yearsOfExperience": 3
}
```

#### `DELETE /api/skills/me/{id}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, e.g. `1`)
- **Request Body:** None

---

### 4. Resume Management (`/api/resumes`)

#### `POST /api/resumes/upload`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `multipart/form-data`
- **Form Data (Body):**
  - `file`: `<resume.pdf>` (Binary PDF file, max 5 MB)

#### `GET /api/resumes`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `GET /api/resumes/download`
- **Auth:** 🔒 Authenticated
- **Query Parameter:** `fileUrl` (String, must start with `https://res.cloudinary.com/`)
- **Request Body:** None
- **Example:**
```http
GET /api/resumes/download?fileUrl=https://res.cloudinary.com/zsypxb04/raw/upload/v1731585600/resumes/sample.pdf
```

#### `PUT /api/resumes/latest/update`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `multipart/form-data`
- **Form Data (Body):**
  - `file`: `<new_resume.pdf>` (Replaces latest uploaded PDF)

---

### 5. AI Resume Review, ATS & Skill Parsing (`/api/resumes`)

#### `POST /api/resumes/{id}/analyze`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Resume ID, e.g. `1`)
- **Request Body:** None
- **Action:** Streams PDF to Python AI service, extracts technical skills across 7 categories, and automatically saves them to the student's skill profile.

#### `POST /api/resumes/{id}/review`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Resume ID, e.g. `1`)
- **Query Parameter (Optional):** `targetCareer` (String)
- **Request Body:** None
- **Example:**
```http
POST /api/resumes/1/review?targetCareer=Backend Developer
```

#### `POST /api/resumes/review-text`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "resume_text": "Prakhar Kumar\nEmail: prakhar@example.com\nSkills: Java, Spring Boot, Docker, PostgreSQL\nExperience: Designed REST APIs handling 50k requests/min. Reduced query latency by 45%...",
  "target_career": "Backend Developer"
}
```

#### `POST /api/resumes/{id}/skill-analysis`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Resume ID, e.g. `1`)
- **Query Parameter (Optional):** `targetCareer` (String)
- **Request Body:** None
- **Example:**
```http
POST /api/resumes/1/skill-analysis?targetCareer=Backend Developer
```

---

### 6. Career Management (`/api/careers`)

#### `POST /api/careers`
- **Auth:** 🛡️ Admin Only
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "name": "Backend Developer",
  "category": "Software Engineering",
  "description": "Builds and maintains core application logic, database schemas, and microservice APIs."
}
```

#### `GET /api/careers`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `GET /api/careers/{id}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, e.g. `1`)
- **Request Body:** None

#### `POST /api/careers/{careerId}/skills`
- **Auth:** 🛡️ Admin Only
- **Path Variable:** `careerId` (Long, e.g. `1`)
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "skillName": "Spring Boot",
  "requiredLevel": "INTERMEDIATE",
  "mandatory": true
}
```

#### `DELETE /api/careers/{careerId}/skills/{skillId}`
- **Auth:** 🛡️ Admin Only
- **Path Variables:** `careerId` (Long), `skillId` (Long)
- **Request Body:** None

---

### 7. Career Recommendations & Semantic Gap Analysis (`/api/recommendations`)

#### `GET /api/recommendations/careers`
- **Auth:** 🔒 Authenticated
- **Request Body:** None
- **Returns:** Ranked list of careers based on the student's current skills.

#### `GET /api/recommendations/careers/{careerId}/skill-gaps`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `careerId` (Long, e.g. `1`)
- **Request Body:** None
- **Returns:** Exact string match percentage and list of missing skills.

#### `GET /api/recommendations/careers/{careerId}/semantic-analysis`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `careerId` (Long, e.g. `1`)
- **Request Body:** None
- **Returns:** **AI-driven deep semantic analysis** via Gemini 3.8 Flash, including category synergies, estimated proficiency, critical vs optional gaps, and prioritized next skills.

---

### 8. Roadmaps & AI Generation (`/api/roadmaps`)

#### `POST /api/roadmaps`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "careerId": 1,
  "title": "My Backend Developer Roadmap"
}
```

#### `GET /api/roadmaps`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `GET /api/roadmaps/{id}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, e.g. `1`)
- **Request Body:** None

#### `PATCH /api/roadmaps/{roadmapId}/items/{itemId}/status`
- **Auth:** 🔒 Authenticated
- **Path Variables:** `roadmapId` (Long), `itemId` (Long)
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```
> Note: `status` can be `NOT_STARTED`, `IN_PROGRESS`, or `COMPLETED`.

#### `POST /api/roadmaps/ai-recommend`
- **Auth:** 🔒 Authenticated
- **Query Parameters:**
  - `careerId`: `1` (Long, Required)
  - `experienceLevel`: `Beginner` (String, Optional, default `Beginner`)
  - `durationWeeks`: `12` (Integer, Optional, default `12`)
- **Request Body:** None
- **Example:**
```http
POST /api/roadmaps/ai-recommend?careerId=1&experienceLevel=Beginner&durationWeeks=12
```

#### `POST /api/roadmaps/ai-generate`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "target_career": "Frontend Developer",
  "current_skills": ["HTML", "CSS", "JavaScript"],
  "experience_level": "Beginner",
  "duration_weeks": 12
}
```

---

### 9. Assessments & Quizzes (`/api/assessments`)

#### `POST /api/assessments`
- **Auth:** 🛡️ Admin Only
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "title": "Java Fundamentals",
  "description": "Test core Java knowledge: OOP, Collections, Concurrency.",
  "skillName": "Java",
  "durationMinutes": 30
}
```

#### `GET /api/assessments`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `POST /api/assessments/{assessmentId}/questions`
- **Auth:** 🛡️ Admin Only
- **Path Variable:** `assessmentId` (Long, e.g. `1`)
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "questionText": "What is the default value of an uninitialized int variable in a Java class?",
  "options": [
    "null",
    "0",
    "undefined",
    "-1"
  ],
  "correctOptionIndex": 1
}
```

#### `GET /api/assessments/{assessmentId}/questions`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `assessmentId` (Long, e.g. `1`)
- **Request Body:** None (Returns questions without revealing `correctOptionIndex`)

#### `POST /api/assessments/{assessmentId}/attempts`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `assessmentId` (Long, e.g. `1`)
- **Request Body:** None (Initializes a new attempt record)

#### `GET /api/assessments/my-attempts`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `POST /api/assessments/attempts/{attemptId}/submit`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `attemptId` (Long, e.g. `1`)
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "answers": [
    {
      "questionId": 1,
      "selectedOptionIndex": 1
    },
    {
      "questionId": 2,
      "selectedOptionIndex": 0
    }
  ]
}
```

#### `GET /api/assessments/attempts/{attemptId}/result`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `attemptId` (Long, e.g. `1`)
- **Request Body:** None

---

### 10. Learning Resources (`/api/resources`)

#### `POST /api/resources`
- **Auth:** 🛡️ Admin Only
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "title": "Spring Boot 3 Deep Dive",
  "url": "https://example.com/courses/spring-boot",
  "type": "VIDEO",
  "skillId": 1,
  "free": true
}
```
> Note: `type` can be `VIDEO`, `ARTICLE`, or `COURSE`.

#### `GET /api/resources`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `GET /api/resources/skill/{skillId}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `skillId` (Long, e.g. `1`)
- **Query Parameter (Optional):** `freeOnly` (boolean, default `false`)
- **Request Body:** None
- **Example:**
```http
GET /api/resources/skill/1?freeOnly=true
```

---

### 11. Progress Tracking (`/api/progress`)

#### `GET /api/progress/summary`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `GET /api/progress/roadmaps/{roadmapId}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `roadmapId` (Long, e.g. `1`)
- **Request Body:** None

---

### 12. Admin Dashboard (`/api/admin`)

#### `GET /api/admin/dashboard`
- **Auth:** 🛡️ Admin Only
- **Request Body:** None

---

# Part 2: Python FastAPI AI Service APIs (Port 8000)

**Base URL:** `http://localhost:8000`

---

### 1. System & Health

#### `GET /`
- **Request Body:** None

#### `GET /health`
- **Request Body:** None

---

### 2. Text Extraction & Skill Parsing

#### `POST /parse-resume`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file)

#### `POST /extract-skills`
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "extracted_text": "Experienced software engineer with expertise in Java, Spring Boot, PostgreSQL, Docker, and AWS microservices."
}
```

#### `POST /analyze-resume`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file)

---

### 3. AI Resume Review & ATS Scoring

#### `POST /resume-review`
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "resume_text": "Prakhar Kumar\nEmail: prakhar@example.com | LinkedIn: linkedin.com/in/prakhar\nSkills: Java, Spring Boot, Docker, PostgreSQL, REST APIs\nExperience: Software Engineer Intern at ABC Tech (2025).\n- Architected high-throughput REST API using Spring Boot, reducing latency by 40%.\n- Deployed containerized microservices to AWS ECS.\nProjects: CareerRoadmap System with full JWT auth and PostgreSQL.",
  "target_career": "Backend Developer"
}
```

#### `POST /resume-review/upload`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file, Required)
  - `target_career`: `Backend Developer` (Text string, Optional)

---

### 4. Dynamic Roadmap Recommendation

#### `POST /roadmap-recommendation`
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "target_career": "Backend Developer",
  "current_skills": [
    "Java",
    "SQL"
  ],
  "experience_level": "Beginner",
  "duration_weeks": 12
}
```

---

### 5. Semantic Skill Analysis

#### `POST /skill-analysis`
- **Content-Type:** `application/json`
- **Request Body (Format A - Explicit skills list):**
```json
{
  "skills": [
    "Java",
    "PostgreSQL",
    "Docker"
  ],
  "target_career": "Backend Developer"
}
```
- **Request Body (Format B - Raw profile text):**
```json
{
  "text": "Full stack enthusiast with 2 years of experience in React, TypeScript, Node.js, and MongoDB.",
  "target_career": "Full Stack Developer"
}
```

#### `POST /skill-analysis/upload`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file, Required)
  - `target_career`: `Backend Developer` (Text string, Optional)
