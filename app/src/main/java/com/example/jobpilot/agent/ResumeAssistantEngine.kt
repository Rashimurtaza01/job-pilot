package com.example.jobpilot.agent

import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResumeJobMatchAnalysis
import com.example.jobpilot.data.model.UserResumeProfile

class ResumeAssistantEngine {

    fun analyzeResumeForJob(profile: UserResumeProfile, job: JobRecord): ResumeJobMatchAnalysis {
        val userSkillsNormalized = profile.skills.map { it.lowercase().trim() }.toSet()
        val jobRequiredNormalized = job.requiredSkills.map { it.lowercase().trim() }
        val jobPreferredNormalized = job.preferredSkills.map { it.lowercase().trim() }

        val matchingSkills = mutableListOf<String>()
        val missingSkills = mutableListOf<String>()

        for (skill in job.requiredSkills) {
            val sNorm = skill.lowercase().trim()
            if (userSkillsNormalized.any { it.contains(sNorm) || sNorm.contains(it) }) {
                matchingSkills.add(skill)
            } else {
                missingSkills.add(skill)
            }
        }

        for (skill in job.preferredSkills) {
            val sNorm = skill.lowercase().trim()
            if (userSkillsNormalized.any { it.contains(sNorm) || sNorm.contains(it) }) {
                if (!matchingSkills.contains(skill)) matchingSkills.add(skill)
            }
        }

        val totalJobSkills = (job.requiredSkills.size + job.preferredSkills.size).coerceAtLeast(1)
        val score = ((matchingSkills.size.toDouble() / totalJobSkills.toDouble()) * 100).toInt().coerceIn(40, 99)

        val experienceAlignment = if (profile.experienceYears >= 3) {
            "Strong Alignment: Your ${profile.experienceYears} years of engineering experience meets or exceeds the baseline requirements for ${job.title}."
        } else {
            "Moderate Alignment: Profile shows ${profile.experienceYears} years experience; emphasize hands-on project accomplishments and open source contributions."
        }

        val educationAlignment = "Aligned: ${profile.education} satisfies the technical degree criteria."

        val suggestedImprovements = mutableListOf<String>()
        if (missingSkills.isNotEmpty()) {
            suggestedImprovements.add("Highlight any indirect exposure or related tools to: ${missingSkills.take(3).joinToString(", ")}.")
        }
        suggestedImprovements.add("Quantify metrics on your resume for ${job.companyName} (e.g. latency improvements, throughput, user adoption).")
        suggestedImprovements.add("Tailor top headline to mirror the requisition title: '${job.title}'.")

        val coverLetter = """
Dear Hiring Team at ${job.companyName},

I am writing to express my strong interest in the ${job.title} role. With a background in ${profile.targetRole} and hands-on expertise in ${matchingSkills.take(4).joinToString(", ")}, I have followed ${job.companyName}'s work in ${job.sector} closely.

In my recent experience, I have focused on building resilient software architectures, collaborating effectively in distributed teams, and delivering high-quality code. I am particularly drawn to ${job.companyName}'s engineering culture and the opportunity to contribute to ${job.responsibilities.firstOrNull() ?: "core engineering initiatives"}.

Thank you for your time and consideration. I welcome the opportunity to discuss how my skill set aligns with your team's goals.

Sincerely,
${profile.fullName}
        """.trimIndent()

        val interviewQuestions = listOf(
            "How have you architected and scaled systems using ${matchingSkills.firstOrNull() ?: "modern technical frameworks"} in production?",
            "Can you describe a challenging bug or performance bottleneck you resolved in a distributed or asynchronous environment?",
            "How do you approach team collaboration, asynchronous code reviews, and technical documentation in a ${job.workArrangement.label.lowercase()} environment?",
            "What strategies would you use at ${job.companyName} to quickly ramp up on ${missingSkills.firstOrNull() ?: "unfamiliar internal tools"}?",
            "Walk us through a time you made an architectural trade-off between delivery speed and long-term maintainability."
        )

        return ResumeJobMatchAnalysis(
            jobId = job.id,
            overallMatchScore = score,
            matchingSkills = matchingSkills,
            missingSkills = missingSkills,
            experienceAlignment = experienceAlignment,
            educationAlignment = educationAlignment,
            suggestedImprovements = suggestedImprovements,
            tailoredCoverLetter = coverLetter,
            interviewPrepQuestions = interviewQuestions
        )
    }

    fun extractSkillsFromText(text: String): List<String> {
        val commonSkills = listOf(
            "Kotlin", "Java", "Python", "Go", "Golang", "Rust", "C++", "C#", "Swift", "TypeScript", "JavaScript",
            "Android", "Jetpack Compose", "Coroutines", "Room", "Retrofit", "Ktor", "Flow", "RxJava",
            "React", "Node.js", "Docker", "Kubernetes", "AWS", "GCP", "Azure", "SQL", "PostgreSQL",
            "Kafka", "gRPC", "REST APIs", "GraphQL", "Redis", "Git", "CI/CD", "Distributed Systems",
            "Microservices", "TDD", "System Design", "DevOps", "Linux"
        )
        val found = mutableListOf<String>()
        val lowerText = text.lowercase()
        for (skill in commonSkills) {
            val sLower = skill.lowercase()
            val regex = Regex("\\b${Regex.escape(sLower)}\\b")
            if (regex.containsMatchIn(lowerText)) {
                found.add(skill)
            }
        }
        return found
    }
}
