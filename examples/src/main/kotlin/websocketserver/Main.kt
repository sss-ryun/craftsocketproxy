package websocketserver

import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.server.ProxyServer

fun main() {
    /**
     * proxyPort - The port the Proxy Server will serve the WebSockets through.
     * host - The host of the game server.
     * port - The port of the game server.
     */
    val config = CraftConnectionConfiguration(80, "localhost", 25565)
    /**
     * config - The configuration above.
     * path - (Optional) The path the Proxy Server will serve the WebSockets through. Default: "/"
     */
    ProxyServer.serve(config, "/minecraft")
}