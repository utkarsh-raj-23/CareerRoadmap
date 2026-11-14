import os
import json
import logging
import concurrent.futures
from typing import Dict, List, Any, Optional
from dotenv import load_dotenv

# Load .env if present
load_dotenv()

logger = logging.getLogger("gemini_service")
logging.basicConfig(level=logging.INFO)

# Check for Google GenAI SDK
try:
    from google import genai
    from google.genai import types
    GENAI_AVAILABLE = True
except ImportError:
    GENAI_AVAILABLE = False
    logger.warning("google-genai package not installed or failed to import.")

# Gemini configuration
DEFAULT_MODEL = os.getenv("GEMINI_CHAT_MODEL") or os.getenv("GEMINI_MODEL", "gemini-3.8-flash")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY") or os.getenv("GOOGLE_API_KEY")
API_TIMEOUT_SECONDS = int(os.getenv("GEMINI_TIMEOUT_SECONDS", "15"))


def get_gemini_client():
    """Initializes and returns a Google GenAI client if credentials are configured."""
    if not GENAI_AVAILABLE:
        return None
    api_key = os.getenv("GEMINI_API_KEY") or os.getenv("GOOGLE_API_KEY")
    if not api_key:
        return None
    try:
        http_options = types.HttpOptions(timeout=15000) if hasattr(types, "HttpOptions") else None
        return genai.Client(api_key=api_key, http_options=http_options)
    except Exception as e:
        logger.error(f"Failed to initialize Gemini Client: {e}")
        return None


def _call_gemini_with_timeout(client, prompt: str, model: str = DEFAULT_MODEL) -> str:
    """Executes Gemini API call with a strict timeout to avoid browser hangs."""
    def _task():
        response = client.models.generate_content(
            model=model,
            contents=prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                temperature=0.2
            )
        )
        return response.text.strip()

    with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
        future = executor.submit(_task)
        return future.result(timeout=API_TIMEOUT_SECONDS)


# ==============================================================================
# FEATURE 1: RESUME REVIEW
# ==============================================================================

def review_resume(
    resume_text: str,
    target_career: Optional[str] = None
) -> Dict[str, Any]:
    """
    Performs deep AI resume review.
    Evaluates ATS score, strengths, weaknesses, section-by-section breakdown,
    and actionable recommendations.
    """
    client = get_gemini_client()
    target_label = target_career if target_career else "Software Engineering / Tech Role"

    if client:
        try:
            prompt = f"""
You are an expert technical recruiter and ATS (Applicant Tracking System) specialist.
Analyze the following resume text specifically for the target role: "{target_label}".

Provide an in-depth, honest, constructive review.
Respond ONLY with a valid JSON object matching the following structure (no markdown fences, no extra text):
{{
  "ats_score": <integer between 0 and 100 representing overall ATS readiness>,
  "target_career": "{target_label}",
  "overall_summary": "<concise 2-3 sentence executive summary of the candidate's profile>",
  "strengths": [
    "<strength 1 with specific observation>",
    "<strength 2 with specific observation>",
    "<strength 3 with specific observation>"
  ],
  "areas_for_improvement": [
    "<critical weakness 1 with actionable remedy>",
    "<critical weakness 2 with actionable remedy>",
    "<critical weakness 3 with actionable remedy>"
  ],
  "section_feedback": {{
    "contact_info": "<evaluation of email, phone, LinkedIn, GitHub links>",
    "summary_or_objective": "<evaluation of professional summary or objective statement>",
    "experience": "<evaluation of bullet points, action verbs, quantified business impact>",
    "projects": "<evaluation of technical complexity, architecture, technologies used>",
    "skills": "<evaluation of categorization, relevance, modern tech keywords>",
    "education": "<evaluation of degree, GPA, relevant coursework, formatting>"
  }},
  "formatting_and_layout_score": <integer between 0 and 100>,
  "impact_and_metrics_score": <integer between 0 and 100>,
  "missing_keywords": [
    "<high-value industry keyword or skill missing for this role>",
    "<keyword 2>",
    "<keyword 3>"
  ],
  "actionable_recommendations": [
    "<specific step 1 to elevate this resume immediately>",
    "<specific step 2>",
    "<specific step 3>",
    "<specific step 4>"
  ]
}}

Resume Content:
\"\"\"
{resume_text[:12000]}
\"\"\"
"""
            text_output = _call_gemini_with_timeout(client, prompt, DEFAULT_MODEL)
            # Clean possible markdown wrapping if any
            if text_output.startswith("```json"):
                text_output = text_output[7:-3].strip()
            elif text_output.startswith("```"):
                text_output = text_output[3:-3].strip()

            data = json.loads(text_output)
            data["engine"] = "gemini"
            data["model"] = DEFAULT_MODEL
            return data

        except Exception as e:
            logger.warning(f"Gemini API call failed for resume review: {e}. Falling back to heuristic engine.")

    # Fallback heuristic review
    return _heuristic_resume_review(resume_text, target_label)


def _heuristic_resume_review(resume_text: str, target_career: str) -> Dict[str, Any]:
    """Smart heuristic resume review when Gemini API key is not configured."""
    lower_text = resume_text.lower()
    word_count = len(resume_text.split())

    # Check basic sections
    has_contact = any(k in lower_text for k in ["linkedin", "github", "@", "phone", "email"])
    has_summary = any(k in lower_text for k in ["summary", "objective", "profile", "about me"])
    has_experience = any(k in lower_text for k in ["experience", "employment", "work history", "internship"])
    has_projects = any(k in lower_text for k in ["projects", "personal projects", "academic projects"])
    has_education = any(k in lower_text for k in ["education", "university", "bachelor", "degree", "b.tech", "cgpa", "gpa"])
    has_skills = any(k in lower_text for k in ["skills", "technical skills", "technologies", "proficiencies"])

    # Metrics check (numbers, percentages, metrics)
    import re
    numbers_found = len(re.findall(r"\b\d+[%+kKmMbB]?\b", resume_text))
    impact_score = min(90, max(40, 45 + numbers_found * 3))

    base_score = 60
    if has_contact: base_score += 6
    if has_experience: base_score += 10
    if has_projects: base_score += 10
    if has_skills: base_score += 8
    if has_education: base_score += 6
    ats_score = min(94, max(45, base_score))

    strengths = []
    if has_projects:
        strengths.append("Contains dedicated project descriptions demonstrating hands-on technical work.")
    if has_skills:
        strengths.append("Clearly lists technical competencies and technical toolchains.")
    if numbers_found > 4:
        strengths.append("Incorporates quantitative metrics and data points within bullet points.")
    else:
        strengths.append("Structured section headers aid programmatic ATS document parsing.")

    areas_for_improvement = []
    if numbers_found <= 3:
        areas_for_improvement.append("Bullet points lack quantifiable metrics (e.g. 'improved latency by 30%', 'served 10k users'). Apply Google's X-Y-Z formula.")
    if not has_summary:
        areas_for_improvement.append("Missing a concise 2-3 line Professional Summary at the top tailoring your value proposition for " + target_career + ".")
    areas_for_improvement.append("Ensure active verbs (Architected, Engineered, Optimized) replace passive descriptions (Worked on, Handled).")

    return {
        "engine": "heuristic_fallback",
        "notice": "Generated via intelligent local parser. Configure GEMINI_API_KEY in .env for full LLM analysis.",
        "ats_score": ats_score,
        "target_career": target_career,
        "overall_summary": f"Resume shows a promising foundation for a {target_career} role, with {word_count} words covering essential technical qualifications.",
        "strengths": strengths,
        "areas_for_improvement": areas_for_improvement,
        "section_feedback": {
            "contact_info": "Verified essential contact signals (email/profiles). Ensure direct clickable GitHub & LinkedIn links are prominent." if has_contact else "Missing clear contact/portfolio links.",
            "summary_or_objective": "Present. Keep it tightly focused on your target specialization." if has_summary else "Missing. Add a 3-line summary summarizing your core tech stack and target role.",
            "experience": "Relevant experiences listed. Strengthen each bullet point with impact verbs and business outcomes." if has_experience else "Limited formal work experience. Elevate your project portfolio to fill this gap.",
            "projects": "Technical projects present. Highlight architecture, database choice, and live demo / GitHub links." if has_projects else "Add at least two end-to-end full-stack or domain-specific projects with deployment links.",
            "skills": "Well-defined technical keywords found. Keep skills grouped into Languages, Frameworks, Cloud, and Tools.",
            "education": "Academic credentials properly outlined with degree and institution." if has_education else "Ensure degree and expected graduation year are clearly visible."
        },
        "formatting_and_layout_score": 82,
        "impact_and_metrics_score": impact_score,
        "missing_keywords": [
            "CI/CD Pipeline",
            "Unit Testing (JUnit / Jest / PyTest)",
            "System Design & Microservices",
            "Docker / Containerization"
        ],
        "actionable_recommendations": [
            "Quantify bullet points: Formula = Accomplished [X] as measured by [Y] by doing [Z].",
            f"Tailor the technical skills section specifically for the target role: {target_career}.",
            "Add GitHub repository links and live deployed demo URLs for your top 2 projects.",
            "Run an ATS readability check to ensure single-column layout without complex nested tables or graphics."
        ]
    }


# ==============================================================================
# FEATURE 2: ROADMAP RECOMMENDATION
# ==============================================================================

def recommend_roadmap(
    target_career: str,
    current_skills: Optional[List[str]] = None,
    experience_level: Optional[str] = "Beginner",
    duration_weeks: Optional[int] = 12
) -> Dict[str, Any]:
    """
    Generates a personalized step-by-step career learning roadmap
    based on student current skills, target career, and timeframe.
    """
    client = get_gemini_client()
    skills_str = ", ".join(current_skills) if current_skills else "None specified"

    if client:
        try:
            prompt = f"""
You are an elite Senior Staff Software Engineer and Tech Career Mentor.
Design a highly structured, personalized {duration_weeks}-week learning roadmap for a student aspiring to become a "{target_career}".

Candidate Profile:
- Target Career: {target_career}
- Current Skills: {skills_str}
- Experience Level: {experience_level}
- Target Timeline: {duration_weeks} weeks

Requirements:
- Organize the roadmap into 3 to 4 sequential phases (e.g. Phase 1: Core Fundamentals, Phase 2: Advanced Topics, Phase 3: Production & Architecture, Phase 4: Capstone Project & Interview Prep).
- For each phase, provide:
  - phase_number (integer)
  - phase_title (string)
  - duration_weeks (integer)
  - objective (string)
  - milestones (list of milestones, each with: milestone_id, title, topics [list of strings], practical_task, recommended_resources [list of strings])
- Include an overall estimate of weekly study hours, career readiness target %, and portfolio project ideas.

Respond ONLY with a valid JSON object matching this schema (no markdown formatting, no comments):
{{
  "target_career": "{target_career}",
  "experience_level": "{experience_level}",
  "total_weeks": {duration_weeks},
  "estimated_hours_per_week": 10,
  "career_readiness_target_percentage": 88,
  "overview": "<2-sentence roadmap strategic roadmap summary>",
  "phases": [
    {{
      "phase_number": 1,
      "phase_title": "<Phase Title>",
      "duration_weeks": 3,
      "objective": "<What the student will achieve in this phase>",
      "milestones": [
        {{
          "milestone_id": 1,
          "title": "<Milestone Title>",
          "topics": ["<Topic 1>", "<Topic 2>", "<Topic 3>"],
          "practical_task": "<Concrete hands-on project or exercise>",
          "recommended_resources": ["<Resource or documentation link/name>"]
        }}
      ]
    }}
  ],
  "capstone_projects": [
    {{
      "project_title": "<Production Capstone Title>",
      "description": "<What to build and key architecture>",
      "tech_stack": ["<Tech 1>", "<Tech 2>", "<Tech 3>"]
    }}
  ],
  "certifications_or_assessments": [
    "<Recommended certification or assessment name>"
  ]
}}
"""
            text_output = _call_gemini_with_timeout(client, prompt, DEFAULT_MODEL)
            if text_output.startswith("```json"):
                text_output = text_output[7:-3].strip()
            elif text_output.startswith("```"):
                text_output = text_output[3:-3].strip()

            data = json.loads(text_output)
            data["engine"] = "gemini"
            data["model"] = DEFAULT_MODEL
            return data

        except Exception as e:
            logger.warning(f"Gemini API call failed for roadmap recommendation: {e}. Using fallback generator.")

    # Fallback heuristic roadmap generator
    return _heuristic_roadmap_recommendation(target_career, current_skills or [], experience_level or "Beginner", duration_weeks or 12)


def _heuristic_roadmap_recommendation(
    target_career: str,
    current_skills: List[str],
    experience_level: str,
    duration_weeks: int
) -> Dict[str, Any]:
    """Smart heuristic roadmap generator tailored to the target career path."""
    career_lower = target_career.lower()

    if "front" in career_lower or "react" in career_lower:
        phases = [
            {
                "phase_number": 1,
                "phase_title": "Modern Frontend Fundamentals",
                "duration_weeks": 3,
                "objective": "Master deep JavaScript (ES6+), semantic HTML5, CSS layout systems (Flexbox, Grid), and responsive design principles.",
                "milestones": [
                    {
                        "milestone_id": 1,
                        "title": "DOM & Asynchronous JavaScript",
                        "topics": ["Promises & Async/Await", "Event Loop & Fetch API", "DOM manipulation & Web Storage"],
                        "practical_task": "Build an interactive Dashboard with API consumption and theme toggling using vanilla JS.",
                        "recommended_resources": ["MDN Web Docs: JavaScript Guide", "javascript.info"]
                    },
                    {
                        "milestone_id": 2,
                        "title": "Modern CSS & Tailwind CSS",
                        "topics": ["CSS Grid & Flexbox", "Tailwind CSS utility classes", "Mobile-first responsive patterns"],
                        "practical_task": "Convert a Figma landing page into pixel-perfect responsive Tailwind UI.",
                        "recommended_resources": ["Tailwind CSS Official Docs", "CSS Tricks Flexbox Guide"]
                    }
                ]
            },
            {
                "phase_number": 2,
                "phase_title": "React.js Ecosystem & State Architecture",
                "duration_weeks": 4,
                "objective": "Develop scalable component hierarchies, state management, routing, and lifecycle hooks.",
                "milestones": [
                    {
                        "milestone_id": 3,
                        "title": "React Core & Custom Hooks",
                        "topics": ["useState, useEffect, useMemo", "Component composition", "Custom Hook authoring"],
                        "practical_task": "Build a multi-step dynamic onboarding form with real-time validation and persistent state.",
                        "recommended_resources": ["react.dev Official Documentation", "Epic React by Kent C. Dodds"]
                    },
                    {
                        "milestone_id": 4,
                        "title": "Global State & Client Routing",
                        "topics": ["React Router v6", "Zustand / Redux Toolkit", "TanStack Query (React Query)"],
                        "practical_task": "Develop an E-commerce product catalog with server-state caching, filtering, and cart drawer.",
                        "recommended_resources": ["TanStack Query Documentation", "Redux Toolkit Essentials"]
                    }
                ]
            },
            {
                "phase_number": 3,
                "phase_title": "Full-Stack React, Performance & Deployment",
                "duration_weeks": duration_weeks - 7 if duration_weeks > 7 else 5,
                "objective": "Build production-ready applications with SSR, Next.js, automated testing, and CI/CD.",
                "milestones": [
                    {
                        "milestone_id": 5,
                        "title": "Next.js App Router & Server Components",
                        "topics": ["Next.js 14+ Server Components", "API Routes & Server Actions", "SEO & Core Web Vitals"],
                        "practical_task": "Build a high-performance content platform with server rendering and authentication.",
                        "recommended_resources": ["Next.js Learn Course", "Vercel Deployment Guide"]
                    },
                    {
                        "milestone_id": 6,
                        "title": "Testing, Accessibility & Production Delivery",
                        "topics": ["Jest & React Testing Library", "WCAG 2.1 Accessibility", "GitHub Actions CI/CD to Vercel"],
                        "practical_task": "Write unit and integration tests with >80% coverage and deploy with custom domain.",
                        "recommended_resources": ["Testing Library Official Guide", "Web.dev Lighthouse Audits"]
                    }
                ]
            }
        ]
        capstone = [
            {
                "project_title": "Collaborative SaaS Project Management Hub",
                "description": "Interactive Kanban boards, drag-and-drop workflow, real-time WebSocket notifications, and role-based permissions.",
                "tech_stack": ["React", "TypeScript", "Tailwind CSS", "Next.js", "Zustand"]
            }
        ]
    elif "data" in career_lower or "machine" in career_lower or "ai" in career_lower:
        phases = [
            {
                "phase_number": 1,
                "phase_title": "Python Data Engineering & Scientific Computing",
                "duration_weeks": 3,
                "objective": "Master numerical computation, tabular data wrangling, and exploratory data analysis.",
                "milestones": [
                    {
                        "milestone_id": 1,
                        "title": "NumPy, Pandas & Advanced Vectorization",
                        "topics": ["DataFrames, GroupBy, Merging", "Time-series indexing", "Vectorized numerical operations"],
                        "practical_task": "Clean and preprocess a 1M+ row Kaggle dataset with outlier detection and imputation.",
                        "recommended_resources": ["Python for Data Analysis (O'Reilly)", "Kaggle Pandas Course"]
                    }
                ]
            },
            {
                "phase_number": 2,
                "phase_title": "Statistical Learning & Classical Machine Learning",
                "duration_weeks": 4,
                "objective": "Train, evaluate, cross-validate, and tune predictive regression and classification pipelines.",
                "milestones": [
                    {
                        "milestone_id": 2,
                        "title": "Scikit-Learn Modeling & Feature Engineering",
                        "topics": ["Linear/Logistic Regression", "Random Forests & XGBoost", "Hyperparameter tuning with Optuna"],
                        "practical_task": "Build an end-to-end churn prediction model with precision-recall curve optimization.",
                        "recommended_resources": ["Scikit-Learn User Guide", "Applied Predictive Modeling"]
                    }
                ]
            },
            {
                "phase_number": 3,
                "phase_title": "Deep Learning, LLMs & Production ML Serving",
                "duration_weeks": duration_weeks - 7 if duration_weeks > 7 else 5,
                "objective": "Deploy neural architectures, embeddings, vector search, and Gemini / LangChain pipelines.",
                "milestones": [
                    {
                        "milestone_id": 3,
                        "title": "PyTorch & Generative AI Systems",
                        "topics": ["PyTorch Neural Networks", "HuggingFace Transformers", "Retrieval-Augmented Generation (RAG)"],
                        "practical_task": "Build an AI-powered document QA system using Gemini API, vector database, and FastAPI.",
                        "recommended_resources": ["DeepLearning.AI Short Courses", "PyTorch Tutorials"]
                    }
                ]
            }
        ]
        capstone = [
            {
                "project_title": "Intelligent Resume-to-Job Matching Engine with RAG",
                "description": "Extracts candidate competencies, computes semantic embeddings, matches open positions, and generates tailored interview prep using Gemini.",
                "tech_stack": ["Python", "FastAPI", "Gemini API", "ChromaDB", "PyTorch"]
            }
        ]
    else:
        # Default / Backend / Software Engineering
        phases = [
            {
                "phase_number": 1,
                "phase_title": "Programming Core & Data Structures",
                "duration_weeks": 3,
                "objective": "Solidify object-oriented programming, algorithmic problem-solving, and clean code principles.",
                "milestones": [
                    {
                        "milestone_id": 1,
                        "title": "OOP & Algorithmic Foundations",
                        "topics": ["OOP Principles (SOLID)", "HashMaps, Trees & Graph Traversals", "Time & Space Complexity"],
                        "practical_task": "Solve 30 curated LeetCode medium questions and implement core data structures from scratch.",
                        "recommended_resources": ["NeetCode Roadmap", "Clean Code by Robert C. Martin"]
                    }
                ]
            },
            {
                "phase_number": 2,
                "phase_title": "Backend Architecture & RESTful API Engineering",
                "duration_weeks": 4,
                "objective": "Construct robust web services with database ORM, JWT security, and validation.",
                "milestones": [
                    {
                        "milestone_id": 2,
                        "title": "Enterprise Backend Frameworks (Spring Boot / FastAPI)",
                        "topics": ["Dependency Injection", "JPA / Hibernate / SQLAlchemy", "JWT Authentication & Security Filters"],
                        "practical_task": "Create a multi-tenant REST API with role-based access control and PostgreSQL persistence.",
                        "recommended_resources": ["Spring.io Official Guides", "FastAPI Documentation"]
                    }
                ]
            },
            {
                "phase_number": 3,
                "phase_title": "Cloud, Microservices & Production Operations",
                "duration_weeks": duration_weeks - 7 if duration_weeks > 7 else 5,
                "objective": "Containerize services, build CI/CD pipelines, and deploy on modern cloud platforms.",
                "milestones": [
                    {
                        "milestone_id": 3,
                        "title": "Docker, Kubernetes & Cloud Deployment",
                        "topics": ["Docker multi-stage builds", "Docker Compose orchestration", "Cloud hosting (AWS / GCP)"],
                        "practical_task": "Containerize backend and database services, set up GitHub Actions CI/CD with automated test runs.",
                        "recommended_resources": ["Docker Getting Started Guide", "AWS Developer Documentation"]
                    }
                ]
            }
        ]
        capstone = [
            {
                "project_title": "High-Throughput Distributed Notification & Task Engine",
                "description": "Event-driven architecture with asynchronous worker queues, rate-limiting, and PostgreSQL audit logging.",
                "tech_stack": ["Java / Python", "Spring Boot / FastAPI", "PostgreSQL", "Redis", "Docker"]
            }
        ]

    return {
        "engine": "heuristic_fallback",
        "notice": "Generated via intelligent career template engine. Add GEMINI_API_KEY in .env for dynamic AI generation.",
        "target_career": target_career,
        "experience_level": experience_level,
        "total_weeks": duration_weeks,
        "estimated_hours_per_week": 12,
        "career_readiness_target_percentage": 85,
        "overview": f"A comprehensive {duration_weeks}-week roadmap engineered to take you from {experience_level} to production-ready {target_career}.",
        "phases": phases,
        "capstone_projects": capstone,
        "certifications_or_assessments": [
            f"Official {target_career} Developer Skill Assessment",
            "Cloud Practitioner / Associate Certification (AWS / GCP)"
        ]
    }


# ==============================================================================
# FEATURE 3: SKILL ANALYSIS
# ==============================================================================

def analyze_skills_deep(
    text_or_skills: Any,
    target_career: Optional[str] = None
) -> Dict[str, Any]:
    """
    Performs deep semantic skill extraction and gap analysis.
    Identifies proficiency levels, categorized domains, match percentage against
    a target career, and prioritized missing skills.
    """
    from skill_extractor import extract_skills as regex_extract

    # Convert input to string if list
    if isinstance(text_or_skills, list):
        input_text = ", ".join([str(s) for s in text_or_skills])
        existing_skills_list = text_or_skills
    else:
        input_text = str(text_or_skills)
        regex_res = regex_extract(input_text)
        existing_skills_list = regex_res.get("all_skills", [])

    target_label = target_career if target_career else "Full Stack Software Engineer"
    client = get_gemini_client()

    if client:
        try:
            prompt = f"""
You are an expert technical talent assessor and software engineering skill taxonomy specialist.
Analyze the following candidate skills and text for the target career role: "{target_label}".

Extract and evaluate:
1. All technical and domain skills found in the profile.
2. Estimated proficiency for each skill ("Beginner", "Intermediate", "Advanced") based on project context, tool usage, or complexity.
3. Categorize into: "Programming Languages", "Backend", "Frontend", "Databases", "DevOps & Cloud", "AI & Data", "Tools & Methodologies", "Soft Skills".
4. Evaluate match percentage (0-100%) against industry standards for "{target_label}".
5. Identify high-priority missing skills required to succeed in "{target_label}".
6. Provide recommended next 3-5 skills to learn in priority order.

Respond ONLY with a valid JSON object matching this schema:
{{
  "target_career": "{target_label}",
  "overall_match_percentage": <integer between 0 and 100>,
  "total_skills_identified": <integer count of all skills>,
  "all_skills": ["<Skill 1>", "<Skill 2>", "<Skill 3>"],
  "skills_with_proficiency": [
    {{
      "name": "<Skill Name>",
      "category": "<Category>",
      "proficiency": "<Beginner | Intermediate | Advanced>",
      "evidence": "<Short justification from context, e.g. Used in 2 full-stack projects>"
    }}
  ],
  "categorized_skills": {{
    "Programming Languages": ["<Skill>"],
    "Backend": ["<Skill>"],
    "Frontend": ["<Skill>"],
    "Databases": ["<Skill>"],
    "DevOps & Cloud": ["<Skill>"],
    "AI & Data": ["<Skill>"],
    "Tools & Methodologies": ["<Skill>"],
    "Soft Skills": ["<Skill>"]
  }},
  "strengths_summary": "<1-2 sentence description of technical strengths>",
  "missing_critical_skills": [
    {{
      "skill": "<Missing Skill>",
      "importance": "<CRITICAL | HIGH | MEDIUM>",
      "reason": "<Why it is required for this role>"
    }}
  ],
  "recommended_learning_priority": [
    "<Skill 1 to learn next>",
    "<Skill 2 to learn next>",
    "<Skill 3 to learn next>"
  ]
}}

Candidate Data / Resume Text:
\"\"\"
{input_text[:12000]}
\"\"\"
"""
            text_output = _call_gemini_with_timeout(client, prompt, DEFAULT_MODEL)
            if text_output.startswith("```json"):
                text_output = text_output[7:-3].strip()
            elif text_output.startswith("```"):
                text_output = text_output[3:-3].strip()

            data = json.loads(text_output)
            data["engine"] = "gemini"
            data["model"] = DEFAULT_MODEL
            return data

        except Exception as e:
            logger.warning(f"Gemini API call failed for skill analysis: {e}. Falling back to heuristic engine.")

    # Fallback heuristic skill analyzer
    return _heuristic_skill_analysis(input_text, target_label)


def _heuristic_skill_analysis(input_text: str, target_career: str) -> Dict[str, Any]:
    """Smart heuristic skill analyzer when Gemini is not active."""
    from skill_extractor import extract_skills

    regex_res = extract_skills(input_text)
    all_skills = regex_res.get("all_skills", [])
    categorized = regex_res.get("categorized_skills", {})

    # Ensure all expected category keys exist
    expected_categories = [
        "Programming Languages", "Backend", "Frontend", "Databases",
        "DevOps & Cloud", "Tools", "Machine Learning"
    ]
    for cat in expected_categories:
        if cat not in categorized:
            categorized[cat] = []

    # Map cloud into DevOps & Cloud if present
    if "Cloud" in categorized and categorized["Cloud"]:
        categorized["DevOps & Cloud"] = list(set(categorized.get("DevOps & Cloud", []) + categorized["Cloud"]))

    # Benchmarks for typical tech careers
    benchmarks = {
        "backend": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Git", "REST API", "Redis", "Kubernetes"],
        "frontend": ["JavaScript", "TypeScript", "React", "HTML", "CSS", "Tailwind CSS", "Git", "Next.js"],
        "full stack": ["JavaScript", "React", "Node.js", "Java", "Spring Boot", "PostgreSQL", "Git", "Docker"],
        "data": ["Python", "Pandas", "NumPy", "Scikit-learn", "PostgreSQL", "Machine Learning", "Git"],
        "cloud": ["AWS", "Docker", "Kubernetes", "Linux", "Git", "Python", "CI/CD"]
    }

    career_lower = target_career.lower()
    selected_bench = benchmarks["full stack"]
    for key, bench in benchmarks.items():
        if key in career_lower:
            selected_bench = bench
            break

    lower_skills = [s.lower() for s in all_skills]
    matched = [s for s in selected_bench if s.lower() in lower_skills]
    missing = [s for s in selected_bench if s.lower() not in lower_skills]

    match_pct = int((len(matched) / max(len(selected_bench), 1)) * 100) if selected_bench else 70
    match_pct = max(35, min(95, match_pct))

    skills_with_prof = []
    for skill in all_skills:
        skills_with_prof.append({
            "name": skill,
            "category": "Technical",
            "proficiency": "Intermediate",
            "evidence": "Identified from project mentions and technical keywords in profile"
        })

    missing_critical = []
    for miss in missing[:4]:
        missing_critical.append({
            "skill": miss,
            "importance": "CRITICAL" if miss in ["Docker", "Java", "React", "PostgreSQL", "Python"] else "HIGH",
            "reason": f"Standard core competency for {target_career} role requirements."
        })

    return {
        "engine": "heuristic_fallback",
        "notice": "Generated via standard skill taxonomy parser. Configure GEMINI_API_KEY in .env for deep AI analysis.",
        "target_career": target_career,
        "overall_match_percentage": match_pct,
        "total_skills_identified": len(all_skills),
        "all_skills": all_skills,
        "skills_with_proficiency": skills_with_prof,
        "categorized_skills": categorized,
        "strengths_summary": f"Demonstrated competencies in {', '.join(all_skills[:4]) if all_skills else 'foundational computing'}." ,
        "missing_critical_skills": missing_critical,
        "recommended_learning_priority": missing[:3] if missing else ["Docker", "Kubernetes", "System Design"]
    }

