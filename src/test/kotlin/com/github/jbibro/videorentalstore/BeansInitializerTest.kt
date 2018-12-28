package com.github.jbibro.videorentalstore

import com.github.jbibro.videorentalstore.rental.usecases.RentFilm
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network.getFreeServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.context.support.registerBean
import java.net.InetAddress.getLocalHost

class BeansInitializerTest : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans().initialize(context)
        context.registerBean<RentFilm>()
        testBeans().initialize(context)
    }
}

/*
We need this because filtered positional operator (https://docs.mongodb.com/manual/reference/operator/update/positional-filtered/)
used in ReturnFilm class was introduced in 3.6 version.
But after changing this version I noticed performance problems when running tests (~3x slower).

I found solution here https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/166#issuecomment-425586048
That's why I had to provide custom IMongodConfig instead of using the one from EmbeddedMongoAutoConfiguration
 */
fun testBeans() = beans {
    bean<IMongodConfig> {
        val config = MongodConfigBuilder().version(Version.V3_6_5)
        config.net(Net("localhost", getFreeServerPort(getLocalHost()), false))
        config.cmdOptions(MongoCmdOptionsBuilder().useStorageEngine("ephemeralForTest").build())
        config.build()
    }
}