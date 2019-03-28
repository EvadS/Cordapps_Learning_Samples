package itravel.plugin

import itravel.api.TravelApi
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function
import net.corda.core.messaging.CordaRPCOps

/**
 * This class defines the plugin for the ELOC application.
 */
class TravelPlugin : WebServerPluginRegistry {
    /**
     * A list of classes that expose web APIs.
     */
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::TravelApi))

    /**
     * A list of directories in the resources directory that will be served by Jetty under /web.
     */
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the exampleWeb directory in resources to /web/example
            "travel" to javaClass.classLoader.getResource("travel").toExternalForm()
    )
}