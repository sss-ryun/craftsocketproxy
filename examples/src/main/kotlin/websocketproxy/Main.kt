package websocketproxy

import me.ryun.mcsockproxy.client.ProxyClient
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration

fun main() {
    /**
     * proxyPort - The port the Proxy Client will serve the game port.
     * host - The host of the game server.
     * port - The port of the game server. If it's 80 or 443, it's automatically detected as a WebSocket proxy.
     * To use it: "localhost:25566".
     */
    val config = CraftConnectionConfiguration(25566, "localhost", 80)
    /**
     * config - The configuration above.
     * path - (Optional) Path to be used. (ws://example.com:80/minecraft)
     */
    ProxyClient.serve(config, "/minecraft")
}