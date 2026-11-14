import re

SKILL_DATABASE = {
    "Programming Languages": [
        "Java",
        "Python",
        "C++",
        "C#",
        "C",
        "JavaScript",
        "TypeScript",
        "PHP",
        "Ruby",
        "Go",
        "Golang",
        "Kotlin",
        "Swift",
        "Rust",
        "SQL",
        "R"
    ],

    "Backend": [
        "Spring Boot",
        "Spring Framework",
        "Spring Security",
        "Node.js",
        "Express.js",
        "Django",
        "Flask",
        "FastAPI",
        "REST API",
        "GraphQL",
        "Hibernate",
        "JDBC",
        "Microservices",
        "Kafka",
        "RabbitMQ"
    ],

    "Frontend": [
        "HTML",
        "HTML5",
        "CSS",
        "CSS3",
        "React",
        "React.js",
        "Next.js",
        "Angular",
        "Vue.js",
        "Bootstrap",
        "Tailwind CSS",
        "Redux",
        "Webpack",
        "Vite"
    ],

    "Databases": [
        "MySQL",
        "PostgreSQL",
        "MongoDB",
        "Oracle",
        "Redis",
        "SQLite",
        "Cassandra",
        "Elasticsearch",
        "DynamoDB"
    ],

    "Tools": [
        "Git",
        "GitHub",
        "GitLab",
        "Docker",
        "Kubernetes",
        "Jenkins",
        "Maven",
        "Gradle",
        "Postman",
        "Linux",
        "Jira",
        "Nginx"
    ],

    "Machine Learning": [
        "Machine Learning",
        "Deep Learning",
        "TensorFlow",
        "PyTorch",
        "Scikit-learn",
        "Pandas",
        "NumPy",
        "Natural Language Processing",
        "NLP",
        "Computer Vision",
        "LLM",
        "LangChain",
        "HuggingFace"
    ],

    "Cloud": [
        "AWS",
        "Azure",
        "Google Cloud",
        "GCP",
        "Firebase",
        "Heroku",
        "Vercel",
        "Netlify"
    ]
}


def extract_skills(text: str):
    """
    Extracts technical skills from text using regex boundary matching.
    Returns categorized skills and a flat list of distinct detected skills.
    """
    if not text:
        return {
            "total_skills_found": 0,
            "all_skills": [],
            "categorized_skills": {}
        }

    extracted_skills = {}
    all_skills = []
    seen = set()

    for category, skills in SKILL_DATABASE.items():
        matched_skills = []

        for skill in skills:
            # Match word boundary, allowing for punctuation in C++, C#, .js, etc.
            if skill in ["C++", "C#"]:
                pattern = r"(?<![A-Za-z0-9])" + re.escape(skill) + r"(?![A-Za-z0-9])"
            else:
                pattern = r"\b" + re.escape(skill) + r"\b"

            if re.search(pattern, text, re.IGNORECASE):
                if skill not in seen:
                    matched_skills.append(skill)
                    all_skills.append(skill)
                    seen.add(skill)

        if matched_skills:
            extracted_skills[category] = matched_skills

    return {
        "total_skills_found": len(all_skills),
        "all_skills": all_skills,
        "categorized_skills": extracted_skills
    }
