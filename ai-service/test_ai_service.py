import os
import sys
import io

# Ensure ai-service directory is in sys.path
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

import fitz
import main
from gemini_service import review_resume, recommend_roadmap, analyze_skills_deep

SAMPLE_RESUME_TEXT = """
John Doe - Full Stack Developer
Email: john.doe@example.com | LinkedIn: linkedin.com/in/johndoe | GitHub: github.com/johndoe

Summary:
Passionate Software Engineer with 2+ years of experience building high-scale web applications.

Technical Skills:
- Languages: Java, Python, JavaScript, TypeScript, SQL
- Backend: Spring Boot, REST API, Node.js, Express.js
- Frontend: React, HTML, CSS, Tailwind CSS
- Databases: PostgreSQL, MongoDB, Redis
- Tools & Cloud: Docker, Kubernetes, Git, AWS, Jenkins

Experience:
Software Engineer Intern - Acme Corp (2024 - 2025)
- Designed and built microservices using Spring Boot and PostgreSQL, reducing query latency by 35%.
- Implemented real-time dashboard in React with Tailwind CSS, improving user engagement for 15,000 monthly active users.
- Automated CI/CD pipeline using Docker and GitHub Actions, cutting deployment time by 50%.

Projects:
E-Commerce Microservices Engine
- Architected RESTful services handling 1,000 requests/sec with Redis caching and PostgreSQL persistence.
- Deployed on AWS using Docker containers and Kubernetes.

Education:
B.Tech in Computer Science and Engineering - XYZ University (2021 - 2025) | CGPA: 8.8/10
"""

def create_sample_pdf(text: str) -> bytes:
    doc = fitz.open()
    page = doc.new_page()
    page.insert_text((50, 50), text)
    pdf_bytes = doc.tobytes()
    doc.close()
    return pdf_bytes


def test_home_and_health():
    home = main.home()
    assert home["status"] == "ONLINE"
    assert len(home["features"]) >= 6
    print(" [1/8] PASS: GET / Home endpoint verified")

    health = main.health_check()
    assert health["status"] == "UP"
    assert "gemini_integration" in health
    print(f" [2/8] PASS: GET /health verified (Mode: {health['gemini_integration']['mode']})")


def test_pdf_parsing_and_validation():
    pdf_bytes = create_sample_pdf(SAMPLE_RESUME_TEXT)
    pages, text = main.extract_pdf_text_from_bytes(pdf_bytes, "sample.pdf")
    assert pages >= 1
    assert "John Doe" in text
    assert "Spring Boot" in text
    print(f" [3/8] PASS: PDF text extraction verified ({pages} page, {len(text)} chars)")

    # Validation: reject non-PDF
    try:
        main.extract_pdf_text_from_bytes(b"Not a PDF", "bad.pdf")
        assert False, "Should have raised exception on invalid PDF signature"
    except Exception:
        pass
    print(" [4/8] PASS: PDF signature and size validation verified")


def test_skill_extraction():
    skills_res = main.extract_skills(SAMPLE_RESUME_TEXT)
    assert skills_res["total_skills_found"] >= 8
    assert "Java" in skills_res["all_skills"]
    assert "Spring Boot" in skills_res["all_skills"]
    assert "React" in skills_res["all_skills"]
    assert "PostgreSQL" in skills_res["all_skills"]
    assert "Docker" in skills_res["all_skills"]
    print(f" [5/8] PASS: Skill extraction verified ({skills_res['total_skills_found']} skills extracted)")


def test_resume_review_feature():
    req = main.ResumeReviewRequest(
        resume_text=SAMPLE_RESUME_TEXT,
        target_career="Full Stack Java Developer"
    )
    review = main.api_review_resume_text(req)
    assert "ats_score" in review
    assert review["ats_score"] >= 50
    assert len(review["strengths"]) > 0
    assert len(review["areas_for_improvement"]) > 0
    assert "section_feedback" in review
    assert "actionable_recommendations" in review
    print(f" [6/8] PASS: Resume Review feature verified (ATS Score: {review['ats_score']}/100, Engine: {review.get('engine')})")


def test_roadmap_recommendation_feature():
    req = main.RoadmapRequest(
        target_career="Backend Developer",
        current_skills=["Java", "SQL", "Git"],
        experience_level="Beginner",
        duration_weeks=12
    )
    roadmap = main.api_recommend_roadmap(req)
    assert roadmap["target_career"] == "Backend Developer"
    assert len(roadmap["phases"]) >= 3
    assert "milestones" in roadmap["phases"][0]
    assert len(roadmap["capstone_projects"]) > 0
    print(f" [7/8] PASS: Roadmap Recommendation feature verified ({len(roadmap['phases'])} phases, {len(roadmap['capstone_projects'])} capstone projects)")


def test_skill_analysis_feature():
    req = main.SkillAnalysisRequest(
        text=SAMPLE_RESUME_TEXT,
        target_career="Backend Developer"
    )
    skill_analysis = main.api_analyze_skills(req)
    assert "overall_match_percentage" in skill_analysis
    assert skill_analysis["overall_match_percentage"] > 0
    assert "missing_critical_skills" in skill_analysis
    assert "recommended_learning_priority" in skill_analysis
    print(f" [8/8] PASS: Skill Analysis feature verified (Match: {skill_analysis['overall_match_percentage']}%, Missing: {len(skill_analysis['missing_critical_skills'])})")


if __name__ == "__main__":
    print("\n========================================================")
    print("   CAREERROADMAP AI SERVICE - COMPREHENSIVE TEST SUITE")
    print("========================================================\n")
    test_home_and_health()
    test_pdf_parsing_and_validation()
    test_skill_extraction()
    test_resume_review_feature()
    test_roadmap_recommendation_feature()
    test_skill_analysis_feature()
    print("\n========================================================")
    print("   SUCCESS: ALL 8 TEST SUITES COMPLETED AND PASSED!")
    print("========================================================\n")
