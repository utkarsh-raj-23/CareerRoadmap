# 🚀 AI Career Assistance — Full Stack Intelligent Career Platform

AI Career Assistance is an enterprise-grade career planning, resume intelligence, and skills acceleration system. It couples a **Spring Boot 4 (Java 21)** microservice backend, an asynchronous **Python FastAPI AI Microservice** powered by **Google Gemini (`gemini-3.8-flash`)**, and a modern **React 19 + Tailwind CSS** dark-themed frontend.

---

## 🏗️ System Architecture

```text
[ React 19 Frontend ]
      │
      │ HTTP / REST & Secure Cookies (career_jwt)
      ▼
[ Spring Boot 4 Backend (Port 10000) ] ────► [ PostgreSQL Database ]
      │                                ────► [ Cloudinary Storage (Resumes) ]
      │
      │ RestClient (HTTP REST)
      ▼
[ Python FastAPI AI Microservice (Port 8000) ]
      │
      ├────► [ Google Gemini 3.8 Flash API ] (Primary Engine with 15s Timeout)
      │
      └────► [ High-Fidelity Heuristic Fallback Engine ] (Zero-Downtime Fallback)
```

### Key Technical Capabilities
1. **AI Resume Review & ATS Scoring**: Evaluates candidate resumes against target roles, generating a 0–100 ATS score, strengths, critical formatting/keyword gaps, and bullet-point optimizations.
2. **Dynamic 12-Week AI Roadmap Recommendation**: Produces phased, actionable milestone roadmaps with estimated completion timelines, curated learning topics, and capstone projects.
3. **Deep Semantic Skill Gap Analysis**: Uses semantic embeddings and domain taxonomies to identify required skills, mark current proficiencies, and flag missing critical competencies.
4. **Interactive Multi-Role Skill Assessments**:
   - **Fresher Role (Core CS)**: 15 questions across OOP, Data Structures & Algorithms, Operating Systems, Computer Networks, and DBMS.
   - **Cloud & DevOps Role**: 15 questions covering Docker, Kubernetes, AWS Cloud Architecture, Terraform (IaC), CI/CD, and Observability.
   - **AI & ML Role**: 15 questions covering Supervised/Unsupervised ML, Deep Learning, NLP Transformers, LoRA, and MLOps.
   - **Cybersecurity Role**: 15 questions covering CIA Triad, OWASP Top 10, Modern Cryptography, Zero Trust, and Incident Response.
   - **Difficulty Tagging**: Questions categorized as `EASY`, `MEDIUM`, or `HARD`.
   - **Post-Submission Analytics**: Total marks, performance percentage, difficulty-wise breakdown (`Easy X/Y`, `Med X/Y`, `Hard X/Y`), topic-wise breakdown, and detailed question review with explanations.

---

## 🛠️ Tech Stack & Directory Structure

- **Backend (`/backend/careerroadmap`)**: Spring Boot 4.1.2, Java 21, Spring Data JPA, Spring Security (JWT via cookie `career_jwt` or `Bearer`), PostgreSQL, Cloudinary HTTP5 SDK, Lombok.
- **AI Microservice (`/ai-service`)**: Python 3.12, FastAPI, Uvicorn, Google GenAI SDK (`google-genai>=3.0.0`), PyMuPDF (`fitz`), Python-Dotenv.
- **Frontend (`/Software Project Frontend/my-app`)**: React 19, Vite 8, Tailwind CSS 4, React Router 7, Lucide React, React Hot Toast.

---

## ⚙️ Environment Configuration

### Spring Boot Backend (`.env` or `application.properties`)
```properties
# Server
server.port=10000
spring.application.name=Career Assistance

# PostgreSQL Database
spring.datasource.url=jdbc:postgresql://localhost:5432/Student
spring.datasource.username=postgres
spring.datasource.password=your_db_password

# JWT
jwt.secret=HbfIjU2f/1l8BVUc/WxVMqUAik3fSKp3jEzx5GlDTUs=
jwt.expiration=86400000
jwt.cookie-name=career_jwt
JWT_COOKIE_SECURE=false
JWT_COOKIE_SAME_SITE=Lax

# Cloudinary Storage
cloudinary.cloud.name=your_cloud_name
cloudinary.api.key=your_cloudinary_key
cloudinary.api.secret=your_cloudinary_secret

# AI Service Microservice Link
ai.service.url=http://localhost:8000
```

### Python AI Microservice (`.env`)
```env
GEMINI_API_KEY=your_gemini_api_key
GEMINI_CHAT_MODEL=gemini-3.8-flash
GEMINI_TIMEOUT_SECONDS=15
PORT=8000
```

---

## 🚀 How to Run the Project

### 1. Start Python AI Microservice
```bash
cd "d:\5th Semester\Software Engineering\CareerRoadmap\ai-service"
# Activate virtual environment
.\.venv\Scripts\activate
# Start FastAPI server
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
*Health Check:* `http://localhost:8000/health`

### 2. Start Spring Boot Backend
```bash
cd "d:\5th Semester\Software Engineering\CareerRoadmap\backend\careerroadmap"
# Compile and run
.\mvnw.cmd spring-boot:run
```
*Running at:* `http://localhost:10000`

### 3. Start React Frontend
```bash
cd "d:\5th Semester\Software Engineering\Software Project Frontend\my-app"
# Install dependencies & run development server
npm install
npm run dev
```
*Running at:* `http://localhost:5173` (or `5174`)

---

# 📖 Complete API Specification & Request Body Reference

## 🔐 Authentication & Headers

- **Public Endpoints**: No authentication required.
- **Protected Endpoints**:
  - Header: `Authorization: Bearer <jwt_token>`
  - **OR** Cookie: `career_jwt=<jwt_token>` (set automatically on login)

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
  "password": "AdminPassword123!"
}
```

#### `POST /api/auth/logout`
- **Auth:** 🔒 Authenticated
- **Request Body:** None (Clears HTTP-only `career_jwt` cookie)

#### `GET /api/auth/me`
- **Auth:** 🔒 Authenticated
- **Request Body:** None (Returns current logged-in user profile & role)

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
- **Request Body:** None (Fetches all skills declared by the student)

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
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file, max 5 MB)

#### `GET /api/resumes`
- **Auth:** 🔒 Authenticated
- **Request Body:** None (Returns metadata and Cloudinary URL of uploaded resumes)

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
- **Form Data:**
  - `file`: `<new_resume.pdf>` (Replaces the latest uploaded PDF on Cloudinary)

---

### 5. AI Resume Review, ATS & Skill Parsing (`/api/resumes`)

#### `POST /api/resumes/{id}/analyze`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, Resume ID)
- **Request Body:** None (Parses skills from PDF and auto-syncs with user profile)

#### `POST /api/resumes/{id}/review`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, Resume ID)
- **Query Parameter (Optional):** `targetCareer` (String, e.g. `Backend Developer`)
- **Request Body:** None
- **Example:**
```http
POST /api/resumes/1/review?targetCareer=Backend+Developer
```

#### `POST /api/resumes/review-text`
- **Auth:** 🔒 Authenticated
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "resume_text": "Prakhar Kumar\nEmail: prakhar@example.com\nSkills: Java, Spring Boot, PostgreSQL, Docker, Git\nProjects: CareerRoadmap with full JWT auth and microservices.",
  "target_career": "Backend Developer"
}
```

#### `POST /api/resumes/{id}/skill-analysis`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, Resume ID)
- **Query Parameter (Optional):** `targetCareer` (String, e.g. `Backend Developer`)
- **Request Body:** None (Performs semantic skill gap analysis on the resume)

---

### 6. Career Management (`/api/careers`)

#### `POST /api/careers`
- **Auth:** 🛡️ Admin Only
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "title": "Backend Developer",
  "description": "Designs, implements, and maintains server-side web applications and microservices.",
  "requiredSkills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "REST APIs"]
}
```

#### `GET /api/careers`
- **Auth:** 🔒 Authenticated
- **Request Body:** None

#### `GET /api/careers/{id}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `id` (Long, Career ID)
- **Request Body:** None

---

### 7. Career Recommendations & Semantic Gap Analysis (`/api/recommendations`)

#### `GET /api/recommendations/careers`
- **Auth:** 🔒 Authenticated
- **Request Body:** None (Matches user's declared profile skills against all careers)

#### `GET /api/recommendations/careers/{careerId}/gaps`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `careerId` (Long, e.g. `1`)
- **Request Body:** None (Rule-based exact keyword skill gap report)

#### `GET /api/recommendations/careers/{careerId}/semantic-analysis`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `careerId` (Long, e.g. `1`)
- **Request Body:** None (Deep Gemini semantic skill analysis and categorized gap breakdown)

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
  "skillId": 1,
  "title": "Spring Boot 3 Deep Dive",
  "description": "Complete masterclass covering REST APIs, JPA, and Spring Security.",
  "url": "https://example.com/courses/spring-boot",
  "provider": "Coursera",
  "resourceType": "VIDEO",
  "free": true
}
```
> Note: `resourceType` can be `VIDEO`, `ARTICLE`, or `COURSE`.

#### `GET /api/resources`
- **Auth:** 🔒 Authenticated
- **Request Body:** None (Returns all curated resources)

#### `GET /api/resources/search`
- **Auth:** 🔒 Authenticated
- **Query Parameters:**
  - `title`: `Spring Boot` (String, Required — case-insensitive partial match)
  - `freeOnly`: `false` (Boolean, Optional, default `false`)
- **Request Body:** None
- **Example:**
```http
GET /api/resources/search?title=Spring+Boot&freeOnly=true
```

#### `GET /api/resources/skill/{skillId}`
- **Auth:** 🔒 Authenticated
- **Path Variable:** `skillId` (Long, e.g. `1`)
- **Query Parameter (Optional):** `freeOnly` (Boolean, default `false`)
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
- **Request Body:** None (Aggregated counts of students, skills, roadmaps, and submissions)

---

# Part 2: Python FastAPI AI Service APIs (Port 8000)

**Base URL:** `http://localhost:8000`

---

### 1. System & Health

#### `GET /`
- **Request Body:** None
- **Response:**
```json
{
  "service": "Career Assistance AI Service",
  "version": "2.0.0",
  "status": "operational",
  "model": "gemini-3.8-flash"
}
```

#### `GET /health`
- **Request Body:** None
- **Response:**
```json
{
  "status": "healthy",
  "gemini_configured": true,
  "model": "gemini-3.8-flash"
}
```

---

### 2. Text Extraction & Skill Parsing

#### `POST /parse-resume`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file)
- **Response:**
```json
{
  "filename": "resume.pdf",
  "total_pages": 1,
  "extracted_text": "Experienced software engineer with expertise in Java, Spring Boot, PostgreSQL..."
}
```

#### `POST /extract-skills`
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "extracted_text": "Experienced software engineer with expertise in Java, Spring Boot, PostgreSQL, Docker, and AWS microservices."
}
```
- **Response:**
```json
{
  "found_skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "AWS"],
  "total_skills_found": 5
}
```

#### `POST /analyze-resume`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file)
- **Response:**
```json
{
  "filename": "resume.pdf",
  "total_pages": 1,
  "extracted_text": "...",
  "found_skills": ["Java", "Spring Boot", "Docker", "PostgreSQL"]
}
```

---

### 3. AI Resume Review & ATS Scoring

#### `POST /resume-review`
- **Content-Type:** `application/json`
- **Request Body:**
```json
{
  "resume_text": "Prakhar Kumar\nEmail: prakhar@example.com | LinkedIn: linkedin.com/in/prakhar\nSkills: Java, Spring Boot, Docker, PostgreSQL, REST APIs\nExperience: Software Engineer Intern at ABC Tech (2025).\n- Architected high-throughput REST API using Spring Boot, reducing latency by 40%.\n- Deployed containerized microservices to AWS ECS.\nProjects: AI Career Assistance with full JWT auth and PostgreSQL.",
  "target_career": "Backend Developer"
}
```
- **Response:**
```json
{
  "ats_score": 88,
  "target_career": "Backend Developer",
  "summary": "Strong backend candidate with practical Spring Boot and Docker exposure.",
  "strengths": [
    "Clear quantifiable metrics in work experience (-40% latency)",
    "Strong core Java and microservices tech stack"
  ],
  "areas_for_improvement": [
    "Add automated testing keywords (JUnit, Mockito, Testcontainers)",
    "Mention CI/CD pipeline implementation details"
  ],
  "extracted_skills": ["Java", "Spring Boot", "Docker", "PostgreSQL", "REST APIs"],
  "recommended_keywords": ["JUnit", "Mockito", "Kubernetes", "Kafka", "CI/CD"],
  "formatting_score": 90,
  "keyword_score": 85,
  "model_used": "gemini-3.8-flash"
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
- **Response:**
```json
{
  "target_career": "Backend Developer",
  "experience_level": "Beginner",
  "duration_weeks": 12,
  "overview": "Comprehensive 12-week roadmap tailored for aspiring Backend Developers.",
  "phases": [
    {
      "phase_number": 1,
      "title": "Java Core & OOP Mastery",
      "duration_weeks": 3,
      "description": "Deep dive into OOP, Collections, Concurrency, and Stream API.",
      "milestones": [
        {
          "title": "OOP & Multithreading",
          "topics": ["Encapsulation", "Polymorphism", "Generics", "Executors"],
          "estimated_hours": 20
        }
      ]
    },
    {
      "phase_number": 2,
      "title": "Spring Boot & Relational Databases",
      "duration_weeks": 4,
      "description": "Build production REST APIs with Spring Data JPA and PostgreSQL.",
      "milestones": [
        {
          "title": "RESTful Architecture & Validation",
          "topics": ["Spring MVC", "DTOs", "JPA Relations", "Flyway"],
          "estimated_hours": 30
        }
      ]
    }
  ],
  "capstone_project": {
    "title": "Scalable Multi-Tenant E-Commerce API",
    "description": "Architect a production-ready microservice with JWT auth, Redis caching, and Stripe payments.",
    "tech_stack": ["Java 21", "Spring Boot 4", "PostgreSQL", "Redis", "Docker"]
  },
  "model_used": "gemini-3.8-flash"
}
```

---

### 5. Semantic Skill Analysis

#### `POST /skill-analysis`
- **Content-Type:** `application/json`
- **Request Body (Format A — Explicit skills list):**
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
- **Request Body (Format B — Raw profile text):**
```json
{
  "text": "Full stack enthusiast with 2 years of experience in React, TypeScript, Node.js, and MongoDB.",
  "target_career": "Full Stack Developer"
}
```
- **Response:**
```json
{
  "target_career": "Backend Developer",
  "candidate_skills": [
    {
      "name": "Java",
      "proficiency": "INTERMEDIATE",
      "domain": "Languages"
    },
    {
      "name": "PostgreSQL",
      "proficiency": "INTERMEDIATE",
      "domain": "Databases"
    },
    {
      "name": "Docker",
      "proficiency": "BEGINNER",
      "domain": "Cloud & DevOps"
    }
  ],
  "missing_critical_skills": [
    {
      "name": "Spring Boot",
      "importance": "CRITICAL",
      "reason": "De-facto enterprise framework for Java backend systems."
    },
    {
      "name": "Distributed Caching (Redis)",
      "importance": "HIGH",
      "reason": "Crucial for high-throughput session and query caching."
    }
  ],
  "skill_domains": {
    "Languages": ["Java"],
    "Databases": ["PostgreSQL"],
    "Cloud & DevOps": ["Docker"]
  },
  "overall_readiness_percentage": 68,
  "executive_summary": "Solid programming foundation with good relational database awareness. High priority gap is enterprise framework mastery (Spring Boot).",
  "model_used": "gemini-3.8-flash"
}
```

#### `POST /skill-analysis/upload`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `file`: `<resume.pdf>` (Binary PDF file, Required)
  - `target_career`: `Backend Developer` (Text string, Optional)

---

## 🎯 Frontend Routes & Features Summary

| Route | Page | Purpose | Access |
|---|---|---|---|
| `/home` | Home Page | Landing page showcasing AI philosophy, feature tabs, and platform capabilities. | 🌐 Public |
| `/login` | Login Page | Secure JWT authentication entry point. | 🌐 Public |
| `/register` | Register Page | Student account onboarding. | 🌐 Public |
| `/profile` | Career Profile | Student command center: AI Roadmaps, ATS Resume Review, Semantic Gap Analysis, My Technical Stack, and Skill Assessments. | 🔒 Protected |
| `/dashboard` | Redirect | Automatically redirects to `/profile`. | 🔒 Protected |

### Built-in 60-Question Skill Assessments
Available directly in the **Skill Assessments** tab with difficulty tags (`EASY`, `MEDIUM`, `HARD`) and comprehensive score analysis:
1. **Fresher Role (Core CS)**: 15 questions covering OOP, DSA, OS, CN, and DBMS.
2. **Cloud & DevOps Role**: 15 questions covering Docker, Kubernetes, AWS, Terraform, CI/CD, and Observability.
3. **AI & ML Role**: 15 questions covering Machine Learning, Deep Learning, NLP Transformers, and MLOps.
4. **Cybersecurity Role**: 15 questions covering CIA Triad, OWASP Top 10, Modern Cryptography, and Zero Trust Architecture.
