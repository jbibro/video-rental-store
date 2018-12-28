package com.github.jbibro.videorentalstore.customer.data

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import java.util.UUID

interface CustomerRepository: ReactiveMongoRepository<Customer, UUID>