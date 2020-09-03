package net.cyclestreets.api

data class VectorMap(
        val id: String,
        val name: String,
        val url: String,
        val parent: String,
        val size: String,
        val lastModified: String
)