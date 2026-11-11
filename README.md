# CareerRoadmap - AI-Powered Career Recommendation & Roadmap System

A full-stack career roadmap and resume parsing platform combining a Spring Boot backend and a Python FastAPI AI service.

---

## 🏗️ Architecture

| Component | Technology | Port | Description |
|-----------|------------|------|-------------|
| **Backend** | Spring Boot, Spring Security, Spring Data JPA, Hibernate, JWT | `8080` | REST API, User Authentication, Database CRUD, AI Integration |
| **AI Service** | FastAPI, PyMuPDF (fitz), Pydantic, Uvicorn | `8000` | Resume PDF text extraction, Skill matching & analysis |
| **Database** | PostgreSQL | `5432` | Relational storage (Database: `Student`) |

---

## ⚙️ Configuration Details

### Database (PostgreSQL)
- **Host**: `localhost:5432`
- **Database**: `Student`
- **Username**: `root` (or `postgres`)
- **Password**: `prakhar123`

### Spring Boot Backend
- Configuration file: `backend/careerroadmap/src/main/resources/application.properties`
- Auto-creates tables using Hibernate `ddl-auto=update`.
- JWT Authentication with 256-bit secure secret.

### AI Service
- Virtual environment: `ai-service/.venv`
- Requirements: `ai-service/requirements.txt`
- Interactive API docs (Swagger): `http://localhost:8000/docs`

---

## 🚀 How to Run

### Option 1: Run All Services Together (One Click)
Double-click `start-all.bat` or run:
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

---

## 🧪 Verification & Testing

- **AI Service Health**: `http://localhost:8000/health`
- **AI Service Swagger UI**: `http://localhost:8000/docs`
- **User Registration**: `POST http://localhost:8080/api/auth/register`
- **User Login**: `POST http://localhost:8080/api/auth/login`
