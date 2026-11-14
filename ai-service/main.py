import os
import sys

# Ensure local modules are found regardless of CWD
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

import fitz
from typing import Optional, List
from pydantic import BaseModel, Field
from dotenv import load_dotenv

from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware

from skill_extractor import extract_skills
from gemini_service import (
    review_resume,
    recommend_roadmap,
    analyze_skills_deep,
    get_gemini_client,
    DEFAULT_MODEL
)

# Load environment variables
load_dotenv()

app = FastAPI(
    title="CareerRoadmap AI Microservice",
    description="Intelligent AI service powered by Google Gemini for resume parsing, deep resume review, personalized roadmap recommendation, and semantic skill analysis.",
    version="2.0.0"
)

# Enable CORS for development & direct frontend access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==============================================================================
# PYDANTIC SCHEMAS
# ==============================================================================

class SkillExtractionRequest(BaseModel):
    extracted_text: str = Field(..., description="Plain text extracted from a resume or profile")


class ResumeReviewRequest(BaseModel):
    resume_text: str = Field(..., description="Resume text to evaluate")
    target_career: Optional[str] = Field(None, description="Target job title or career path (e.g. Backend Developer)")


class RoadmapRequest(BaseModel):
    target_career: str = Field(..., description="Desired career path (e.g. Full Stack Developer, Data Scientist)")
    current_skills: Optional[List[str]] = Field(default=[], description="List of skills the user already knows")
    experience_level: Optional[str] = Field("Beginner", description="Beginner, Intermediate, or Advanced")
    duration_weeks: Optional[int] = Field(12, ge=4, le=52, description="Target roadmap duration in weeks (4-52)")


class SkillAnalysisRequest(BaseModel):
    text: Optional[str] = Field(None, description="Resume or profile text")
    skills: Optional[List[str]] = Field(None, description="Explicit list of skills")
    target_career: Optional[str] = Field(None, description="Target career role for gap analysis")


# ==============================================================================
# HELPER FUNCTIONS
# ==============================================================================

def extract_pdf_text_from_bytes(contents: bytes, filename: str) -> tuple[int, str]:
    """Extracts text from PDF bytes using PyMuPDF (fitz)."""
    if len(contents) > 5 * 1024 * 1024:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="File size cannot exceed 5 MB"
        )

    if not contents.startswith(b"%PDF-"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid PDF file signature"
        )

    try:
        document = fitz.open(stream=contents, filetype="pdf")
        extracted_text = ""
        for page_number, page in enumerate(document, start=1):
            page_text = page.get_text()
            extracted_text += f"\n--- Page {page_number} ---\n" + page_text

        total_pages = len(document)
        document.close()

        if not extracted_text.strip():
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail="No text found in PDF. This may be an image-only scanned document."
            )

        return total_pages, extracted_text.strip()

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unable to read PDF file: {str(e)}"
        )


# ==============================================================================
# SYSTEM & HEALTH ENDPOINTS
# ==============================================================================

@app.get("/")
def home():
    gemini_client = get_gemini_client()
    return {
        "service": "CareerRoadmap AI Microservice",
        "version": "2.0.0",
        "status": "ONLINE",
        "gemini_active": bool(gemini_client),
        "model": DEFAULT_MODEL,
        "features": [
            "1. Resume Parsing & Text Extraction (/parse-resume)",
            "2. Skill Extraction (/extract-skills)",
            "3. Full Resume Analysis with Skills (/analyze-resume)",
            "4. Deep AI Resume Review & ATS Score (/resume-review & /resume-review/upload)",
            "5. Dynamic Learning Roadmap Recommendation (/roadmap-recommendation)",
            "6. Semantic Skill & Gap Analysis (/skill-analysis & /skill-analysis/upload)"
        ]
    }


@app.get("/health")
def health_check():
    gemini_client = get_gemini_client()
    return {
        "status": "UP",
        "service": "AI Resume & Roadmap Engine",
        "version": "2.0.0",
        "gemini_integration": {
            "connected": bool(gemini_client),
            "model": DEFAULT_MODEL,
            "mode": "Live Gemini AI" if gemini_client else "High-Fidelity Heuristic Fallback"
        }
    }


# ==============================================================================
# 1. RESUME PARSING & EXTRACTION (EXISTING & BACKWARD-COMPATIBLE)
# ==============================================================================

@app.post("/parse-resume")
async def parse_resume(file: UploadFile = File(...)):
    """Extracts raw text from an uploaded PDF resume."""
    if not file.filename or not file.filename.lower().endswith(".pdf"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only PDF files are allowed"
        )

    contents = await file.read()
    total_pages, extracted_text = extract_pdf_text_from_bytes(contents, file.filename)
    await file.close()

    return {
        "filename": file.filename,
        "total_pages": total_pages,
        "extracted_text": extracted_text
    }


@app.post("/extract-skills")
def extract_resume_skills(request: SkillExtractionRequest):
    """Extracts technical skills from raw resume text."""
    if not request.extracted_text.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Resume text cannot be empty"
        )

    return extract_skills(request.extracted_text)


@app.post("/analyze-resume")
async def analyze_resume(file: UploadFile = File(...)):
    """
    Combined PDF parsing and skill analysis.
    Preserves exact backward-compatibility with Spring Boot's ResumeAnalysisResponse DTO,
    while enriching with Gemini AI summary and review score.
    """
    if not file.filename or not file.filename.lower().endswith(".pdf"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only PDF files are allowed"
        )

    contents = await file.read()
    total_pages, extracted_text = extract_pdf_text_from_bytes(contents, file.filename)
    await file.close()

    # Extract skills
    skills_result = extract_skills(extracted_text)

    # Generate quick AI review summary
    quick_review = review_resume(extracted_text)

    return {
        "filename": file.filename,
        "total_pages": total_pages,
        "extracted_text": extracted_text,
        "skills_analysis": skills_result,
        "ai_review": {
            "ats_score": quick_review.get("ats_score"),
            "summary": quick_review.get("overall_summary"),
            "strengths": quick_review.get("strengths", [])[:3],
            "areas_for_improvement": quick_review.get("areas_for_improvement", [])[:3]
        }
    }


# ==============================================================================
# 2. FEATURE 1: DEEP RESUME REVIEW
# ==============================================================================

@app.post("/resume-review")
def api_review_resume_text(request: ResumeReviewRequest):
    """
    Comprehensive AI Resume Review from text.
    Evaluates ATS score, formatting, impact, strengths, weaknesses, and actionable steps.
    """
    if not request.resume_text.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Resume text cannot be empty"
        )

    return review_resume(request.resume_text, request.target_career)


@app.post("/resume-review/upload")
async def api_review_resume_upload(
    file: UploadFile = File(...),
    target_career: Optional[str] = Form(None)
):
    """
    Comprehensive AI Resume Review directly from PDF upload.
    """
    if not file.filename or not file.filename.lower().endswith(".pdf"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only PDF files are allowed"
        )

    contents = await file.read()
    total_pages, extracted_text = extract_pdf_text_from_bytes(contents, file.filename)
    await file.close()

    result = review_resume(extracted_text, target_career)
    result["filename"] = file.filename
    result["total_pages"] = total_pages
    return result


# ==============================================================================
# 3. FEATURE 2: ROADMAP RECOMMENDATION
# ==============================================================================

@app.post("/roadmap-recommendation")
def api_recommend_roadmap(request: RoadmapRequest):
    """
    Personalized Step-by-Step Career Learning Roadmap.
    Generates multi-phase milestones, topics, practical tasks, resources, and capstone projects
    tailored to the student's current skills, experience level, and target career.
    """
    if not request.target_career.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Target career path is required"
        )

    return recommend_roadmap(
        target_career=request.target_career.strip(),
        current_skills=request.current_skills,
        experience_level=request.experience_level or "Beginner",
        duration_weeks=request.duration_weeks or 12
    )


# ==============================================================================
# 4. FEATURE 3: SKILL ANALYSIS & GAP BREAKDOWN
# ==============================================================================

@app.post("/skill-analysis")
def api_analyze_skills(request: SkillAnalysisRequest):
    """
    Deep Semantic Skill Analysis & Gap Identification.
    Extracts skills, estimates proficiency, categorizes into tech domains,
    calculates match percentage against target career, and identifies missing critical skills.
    """
    text_content = request.text or ""
    if not text_content and request.skills:
        text_content = ", ".join(request.skills)

    if not text_content.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Either text or a list of skills must be provided"
        )

    return analyze_skills_deep(
        text_or_skills=text_content,
        target_career=request.target_career
    )


@app.post("/skill-analysis/upload")
async def api_analyze_skills_upload(
    file: UploadFile = File(...),
    target_career: Optional[str] = Form(None)
):
    """
    Deep Semantic Skill Analysis directly from PDF resume upload.
    """
    if not file.filename or not file.filename.lower().endswith(".pdf"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only PDF files are allowed"
        )

    contents = await file.read()
    total_pages, extracted_text = extract_pdf_text_from_bytes(contents, file.filename)
    await file.close()

    result = analyze_skills_deep(
        text_or_skills=extracted_text,
        target_career=target_career
    )
    result["filename"] = file.filename
    result["total_pages"] = total_pages
    return result


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
