package com.github.jbibro.videorentalstore.film.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "films")
@TypeAlias("film")
data class Film(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val type: Type
)

enum class Type {
    NEW_RELEASE, REGULAR, OLD
}