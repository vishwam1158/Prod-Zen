package com.viz.prodzen.data.managers

import com.viz.prodzen.data.local.entities.AppCategory

object CategoryManager {

    // Predefined categories with colors
    val DEFAULT_CATEGORIES = listOf(
        AppCategory(
            id = 1,
            name = "Social Media",
            color = "#E91E63", // Pink
            iconName = "group",
            dailyLimitMinutes = 60
        ),
        AppCategory(
            id = 2,
            name = "Entertainment",
            color = "#9C27B0", // Purple
            iconName = "movie",
            dailyLimitMinutes = 90
        ),
        AppCategory(
            id = 3,
            name = "Games",
            color = "#FF5722", // Deep Orange
            iconName = "sports_esports",
            dailyLimitMinutes = 60
        ),
        AppCategory(
            id = 4,
            name = "Productivity",
            color = "#4CAF50", // Green
            iconName = "work",
            dailyLimitMinutes = 0
        ),
        AppCategory(
            id = 5,
            name = "Communication",
            color = "#2196F3", // Blue
            iconName = "chat",
            dailyLimitMinutes = 120
        ),
        AppCategory(
            id = 6,
            name = "News & Reading",
            color = "#FF9800", // Orange
            iconName = "article",
            dailyLimitMinutes = 60
        ),
        AppCategory(
            id = 7,
            name = "Shopping",
            color = "#795548", // Brown
            iconName = "shopping_cart",
            dailyLimitMinutes = 45
        ),
        AppCategory(
            id = 8,
            name = "Utilities",
            color = "#607D8B", // Blue Grey
            iconName = "build",
            dailyLimitMinutes = 0
        ),
        AppCategory(
            id = 9,
            name = "Health & Fitness",
            color = "#8BC34A", // Light Green
            iconName = "fitness_center",
            dailyLimitMinutes = 0
        ),
        AppCategory(
            id = 10,
            name = "Others",
            color = "#9E9E9E", // Grey
            iconName = "apps",
            dailyLimitMinutes = 0
        )
    )

    // Auto-categorization rules based on package name patterns
    private val categoryRules = mapOf(
        1 to listOf( // Social Media
            "facebook", "instagram", "twitter", "tiktok", "snapchat",
            "linkedin", "pinterest", "reddit", "tumblr", "whatsapp.w4b"
        ),
        2 to listOf( // Entertainment
            "youtube", "netflix", "spotify", "twitch", "hulu",
            "disney", "prime", "music", "video", "stream"
        ),
        3 to listOf( // Games
            "game", "play", "pubg", "freefire", "clash", "candy",
            "minecraft", "roblox", "supercell", "zynga"
        ),
        4 to listOf( // Productivity
            "office", "docs", "sheets", "drive", "notion", "evernote",
            "trello", "slack", "zoom", "teams", "calendar"
        ),
        5 to listOf( // Communication
            "whatsapp", "telegram", "messenger", "signal", "skype",
            "viber", "wechat", "line", "discord"
        ),
        6 to listOf( // News & Reading
            "news", "medium", "feedly", "flipboard", "pocket",
            "kindle", "books", "reader", "rss"
        ),
        7 to listOf( // Shopping
            "amazon", "flipkart", "myntra", "ebay", "alibaba",
            "shop", "mall", "store", "cart"
        ),
        8 to listOf( // Utilities
            "calculator", "clock", "calendar", "weather", "maps",
            "files", "cleaner", "battery", "settings"
        ),
        9 to listOf( // Health & Fitness
            "health", "fitness", "workout", "yoga", "meditation",
            "tracker", "step", "calorie", "diet"
        )
    )

    /**
     * Auto-categorize app based on package name
     * Returns category ID or 10 (Others) if no match
     */
    fun categorizeApp(packageName: String, appName: String): Int {
        val searchText = "$packageName $appName".lowercase()

        categoryRules.forEach { (categoryId, keywords) ->
            if (keywords.any { keyword -> searchText.contains(keyword) }) {
                return categoryId
            }
        }

        return 10 // Others
    }

    /**
     * Get category name by ID
     */
    fun getCategoryName(categoryId: Int): String {
        return DEFAULT_CATEGORIES.find { it.id == categoryId }?.name ?: "Others"
    }

    /**
     * Get category color by ID
     */
    fun getCategoryColor(categoryId: Int): String {
        return DEFAULT_CATEGORIES.find { it.id == categoryId }?.color ?: "#9E9E9E"
    }
}

