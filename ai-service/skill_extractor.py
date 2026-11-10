
import re


SKILL_DATABASE = {

    "Programming Languages": [
        "Java",
        "Python",
        "C++",
        "C#",
        "JavaScript",
        "TypeScript",
        "PHP",
        "Ruby",
        "Go",
        "Kotlin",
        "Swift"
    ],

    "Backend": [
        "Spring Boot",
        "Spring Framework",
        "Node.js",
        "Express.js",
        "Django",
        "Flask",
        "FastAPI",
        "REST API",
        "Hibernate",
        "JDBC"
    ],

    "Frontend": [
        "HTML",
        "CSS",
        "React",
        "Angular",
        "Vue.js",
        "Bootstrap",
        "Tailwind CSS"
    ],

    "Databases": [
        "MySQL",
        "PostgreSQL",
        "MongoDB",
        "Oracle",
        "Redis",
        "SQLite"
    ],

    "Tools": [
        "Git",
        "GitHub",
        "Docker",
        "Kubernetes",
        "Jenkins",
        "Maven",
        "Postman",
        "Linux"
    ],

    "Machine Learning": [
        "Machine Learning",
        "Deep Learning",
        "TensorFlow",
        "PyTorch",
        "Scikit-learn",
        "Pandas",
        "NumPy",
        "Natural Language Processing"
    ],

    "Cloud": [
        "AWS",
        "Azure",
        "Google Cloud",
        "Firebase"
    ]
}


def extract_skills(text: str):

    extracted_skills = {}

    all_skills = []

    for category, skills in SKILL_DATABASE.items():

        matched_skills = []

        for skill in skills:

            # Match the complete skill name
            pattern = (
                r"(?<![A-Za-z0-9])"
                + re.escape(skill)
                + r"(?![A-Za-z0-9])"
            )

            if re.search(pattern, text, re.IGNORECASE):

                matched_skills.append(skill)
                all_skills.append(skill)

        if matched_skills:

            extracted_skills[category] = matched_skills

    return {
        "total_skills_found": len(all_skills),
        "all_skills": all_skills,
        "categorized_skills": extracted_skills
    }
