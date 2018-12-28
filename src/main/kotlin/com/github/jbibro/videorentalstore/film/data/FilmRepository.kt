package com.github.jbibro.videorentalstore.film.data

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import java.util.UUID

interface FilmRepository: ReactiveMongoRepository<Film, UUID>