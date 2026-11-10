
import fitz
from pydantic import BaseModel
from skill_extractor import extract_skills

from fastapi import FastAPI, UploadFile, File, HTTPException


app = FastAPI(
    title="AI Career Roadmap Service",
    description="AI service for resume parsing and career recommendations",
    version="1.0.0"
)


@app.get("/")
def home():
    return {
        "message": "AI Career Roadmap Service is running"
    }


@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "AI Resume Parser"
    }


@app.post("/parse-resume")
async def parse_resume(file: UploadFile = File(...)):

    # Check whether a file was uploaded
    if not file.filename:
        raise HTTPException(
            status_code=400,
            detail="Please upload a PDF file"
        )

    # Check file extension
    if not file.filename.lower().endswith(".pdf"):
        raise HTTPException(
            status_code=400,
            detail="Only PDF files are allowed"
        )

    # Read uploaded file
    contents = await file.read()

    # Check file size (5 MB)
    if len(contents) > 5 * 1024 * 1024:
        raise HTTPException(
            status_code=400,
            detail="File size cannot exceed 5 MB"
        )

    # Check PDF signature
    if not contents.startswith(b"%PDF-"):
        raise HTTPException(
            status_code=400,
            detail="Invalid PDF file"
        )

    try:
        # Open PDF from memory
        document = fitz.open(
            stream=contents,
            filetype="pdf"
        )

        # Extract text from every page
        extracted_text = ""

        for page_number, page in enumerate(document, start=1):

            page_text = page.get_text()

            extracted_text += (
                f"\n--- Page {page_number} ---\n"
                + page_text
            )

        total_pages = len(document)

        document.close()

        # Check whether text was extracted
        if not extracted_text.strip():
            raise HTTPException(
                status_code=422,
                detail="No text found. This may be a scanned PDF."
            )

        return {
            "filename": file.filename,
            "total_pages": total_pages,
            "extracted_text": extracted_text.strip()
        }

    except HTTPException:
        raise

    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Unable to read PDF. The file may be corrupted or invalid."
        )

    finally:
        await file.close()


class SkillExtractionRequest(BaseModel):
    extracted_text: str


@app.post("/extract-skills")
def extract_resume_skills(request: SkillExtractionRequest):

    if not request.extracted_text.strip():

        raise HTTPException(
            status_code=400,
            detail="Resume text cannot be empty"
        )

    result = extract_skills(request.extracted_text)

    return result


@app.post("/analyze-resume")
async def analyze_resume(file: UploadFile = File(...)):

    try:
        # Validate filename
        if not file.filename or not file.filename.lower().endswith(".pdf"):
            raise HTTPException(
                status_code=400,
                detail="Only PDF files are allowed"
            )

        # Read uploaded PDF
        contents = await file.read()

        # Validate file size
        if len(contents) > 5 * 1024 * 1024:
            raise HTTPException(
                status_code=400,
                detail="File size cannot exceed 5 MB"
            )

        # Validate PDF signature
        if not contents.startswith(b"%PDF-"):
            raise HTTPException(
                status_code=400,
                detail="Invalid PDF file"
            )

        # Open PDF using PyMuPDF
        document = fitz.open(
            stream=contents,
            filetype="pdf"
        )

        # Extract text from all pages
        extracted_text = ""

        for page_number, page in enumerate(document, start=1):

            page_text = page.get_text()

            extracted_text += (
                f"\n--- Page {page_number} ---\n"
                + page_text
            )

        total_pages = len(document)

        document.close()

        # Check extracted text
        if not extracted_text.strip():
            raise HTTPException(
                status_code=422,
                detail="No text found. This may be a scanned PDF."
            )

        # Extract technical skills
        skills_result = extract_skills(extracted_text)

        # Return complete analysis
        return {
            "filename": file.filename,
            "total_pages": total_pages,
            "extracted_text": extracted_text.strip(),
            "skills_analysis": skills_result
        }

    except HTTPException:
        raise

    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Unable to analyze PDF"
        )

    finally:
        await file.close()

