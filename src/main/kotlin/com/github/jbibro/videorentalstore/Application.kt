package com.github.jbibro.videorentalstore

import com.github.jbibro.videorentalstore.customer.CustomerApi
import com.github.jbibro.videorentalstore.customer.CustomerHandler
import com.github.jbibro.videorentalstore.customer.usecases.ReceiveBonusPoints
import com.github.jbibro.videorentalstore.film.FilmApi
import com.github.jbibro.videorentalstore.film.FilmHandler
import com.github.jbibro.videorentalstore.rental.RentalApi
import com.github.jbibro.videorentalstore.rental.RentalHandler
import com.github.jbibro.videorentalstore.rental.usecases.RentFilm
import com.github.jbibro.videorentalstore.rental.usecases.ReturnFilm
import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.context.support.registerBean

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans().initialize(context)
        context.registerBean<RentFilm>()
    }
}

fun beans() = beans {
    // film
    bean {
        FilmHandler(ref())
    }
    bean {
        FilmApi(ref()).router()
    }

    // customer
    bean<ReceiveBonusPoints>()
    bean {
        CustomerHandler(ref())
    }
    bean {
        CustomerApi(ref()).router()
    }

    // rental
    bean {
        ReturnFilm(ref<MongoClient>().getDatabase(ref<MongoProperties>().mongoClientDatabase), ref())
    }
    bean {
        RentalHandler(ref(), ref(), ref())
    }
    bean {
        RentalApi(ref()).router()
    }
}

