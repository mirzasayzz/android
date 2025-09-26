package com.androidai.browser.data

data class AiLink(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val category: String,
    val isFavorite: Boolean = false
)

object AiLinksData {
    fun getDefaultLinks(): List<AiLink> = listOf(
        AiLink("1", "ChatGPT", "OpenAI's conversational AI", "https://chatgpt.com/?model=gpt-4o-mini", "Chatbots"),
        AiLink("2", "Claude", "Anthropic's AI assistant", "https://claude.ai/new", "Chatbots"),
        AiLink("3", "Gemini", "Google's multimodal AI", "https://gemini.google.com/app", "Chatbots"),
        AiLink("4", "Perplexity", "AI search engine", "https://www.perplexity.ai", "Chatbots"),
        AiLink("5", "Poe", "Quora's AI platform", "https://poe.com", "Chatbots"),
        AiLink("6", "Jasper", "AI content creation", "https://www.jasper.ai", "Writing"),
        AiLink("7", "Copy.ai", "AI copywriter", "https://www.copy.ai", "Writing"),
        AiLink("8", "GitHub Copilot", "AI pair programmer", "https://github.com/features/copilot", "Coding"),
        AiLink("9", "Cursor", "AI code editor", "https://www.cursor.so", "Coding"),
        AiLink("10", "Midjourney", "AI image generation", "https://www.midjourney.com", "Image"),
        AiLink("11", "DALL-E", "OpenAI image AI", "https://labs.openai.com", "Image"),
        AiLink("12", "Stable Diffusion", "Open source image AI", "https://stablediffusionweb.com", "Image"),
        AiLink("13", "Udio", "AI music generation", "https://www.udio.com", "Music"),
        AiLink("14", "Suno", "AI music creator", "https://suno.com", "Music"),
        AiLink("15", "Gamma", "AI presentation maker", "https://gamma.app", "Productivity"),
        AiLink("16", "Tome", "AI storytelling", "https://tome.app", "Productivity")
    )
}
