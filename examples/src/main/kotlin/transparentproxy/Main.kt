package transparentproxy

import me.ryun.mcsockproxy.client.ProxyClient
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration

fun main() {
    /**
     * proxyPort - The port the Proxy Client will serve the game port.
     * host - The host of the game server.
     * port - The port of the game server.
     * To use it: "localhost:25566".
     */
    val config = CraftConnectionConfiguration(25566, "localhost", 25565)
    /**
     * config - The configuration above.
     */
    ProxyClient.serve(config)
}