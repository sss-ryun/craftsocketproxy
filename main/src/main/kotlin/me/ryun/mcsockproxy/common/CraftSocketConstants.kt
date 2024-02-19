package me.ryun.mcsockproxy.common

internal class CraftSocketConstants private constructor() {
    companion object {
        const val CONNECTED_CLIENT = "Minecraft Client connected."
        const val CONNECTED_TRANSPARENT = "Connected as a Transparent Minecraft Proxy."
        const val CONNECTED_WEBSOCKET = "Connected as a WebSocket Proxy."
        const val CONNECTION_ATTEMPT = "Attempting connection..."
        const val CONNECTION_BIND = "Failed to bind to one of the selected ports."
        const val CONNECTION_FAILED = "Failed to connect to host."
        const val CONNECTION_REFUSED = "Could not connect to the game server."
        const val CONNECTION_RESTART = "Restarting..."
        const val CONNECTION_TERMINATED = "Connection terminated."
        const val CONNECTION_WAITING = "Not connected yet."
        const val DISCONNECTED_CLIENT = "Minecraft Client disconnected."
        const val DISCONNECTED_WEBSOCKET = "WebSocket disconnected."
        const val HANDSHAKE_COMPLETE = "WebSocket handshake complete."
        const val HANDSHAKE_FAILED = "WebSocket handshake failed."
        const val PLAYER_CONNECTED = "Player connected."
        const val PLAYER_DISCONNECTED = "Player disconnected."
        const val PLAYER_TERMINATED = "Player connection terminated."
        const val PLAYER_TERMINATED_WEBSOCKET = "Player WebSocket connection terminated."
        const val PROXYING = "Proxying"
        const val SHUTTING_DOWN = "Shutting down..."
        const val TOO_MANY_RESTARTS = "Too many failed restart attempts."
        const val UNSUPPORTED_HTTP_RESPONSE = "Unsupported HTTP response!"
        const val UNSUPPORTED_WEBSOCKET_FRAME = "Unsupported frame received! Disconnected."
    }
}