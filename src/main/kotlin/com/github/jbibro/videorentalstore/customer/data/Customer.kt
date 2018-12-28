package com.github.jbibro.videorentalstore.customer.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document(collection = "customers")
@TypeAlias("customer")
class Customer(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val bonusPoints: Int = 0
)