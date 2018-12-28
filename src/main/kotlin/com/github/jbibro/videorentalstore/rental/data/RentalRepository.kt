package com.github.jbibro.videorentalstore.rental.data

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import java.util.UUID

interface RentalRepository: ReactiveMongoRepository<Rental, UUID>