# Craft Socket Proxy
Tunnel Minecraft Servers to a different port or proxy a Minecraft Server through WebSockets.

## Supports
* All Minecraft Versions.
* Any Minecraft Server. (Possibly for other games too.)
* WebSockets.

## Abilities
* Use a CDN like Cloudflare to proxy your Minecraft server.
* Change a Minecraft Server's port number by proxying it.

## Drawbacks
* Added latency depending on how far the client is from the CDN. (if you're proxying through them.)
* Not natively supported in Vanilla Minecraft Java. (Plugins and Mods for support will be developed in the future.)

## Why use this?
* IPv6 to IPv4 and vice-versa address translation for CDNs like Cloudflare. (Don't want to pay for IPv4? Go IPv6 and use WebSockets!)
* It works like a charm.
* DDOS protection if your server is behind a CDN(Cloudflare) proxy.
* Hide your server IP. (Same as above.)
* There is no difference in gameplay except for the added latency. (Which is subjectively unimportant in Minecraft.)
* Guaranteed to support your Minecraft Server no matter how old or how lazy you are on updating in the future.
* Works for Multiplayer Servers.
* Vanilla players won't even know others are in WebSocket proxy unless they are told.
* Data is never processed. This software just shoves it along, making it very efficient, performant, and memory-saving.

## Get Started

Start off by downloading a build from [Releases](https://github.com/BedsAreDragons/craftsocketproxy/releases/).

### Prerequisites (Requirements)
* At least Java 17+ (Download Available from https://www.openlogic.com/openjdk-downloads?field_java_parent_version_target_id=807&field_operating_system_target_id=436&field_architecture_target_id=391&field_java_package_target_id=401)
* Terminal

### Run the jar with the following commands

**Proxy Client (no WebSockets)** `localhost:25565 -> localhost:25566`
```bash
java -jar CraftSocketProxy-1.1.3.jar --c -host localhost -port 25565 -proxy 25566
```
> [!NOTE]
> This one is useful if you can't change the port forwarding settings in your router, and you can't change the server port.
> Simply proxy it to a different port.

**Proxy Client (with WebSockets)** `ws://example.com:80 -> localhost:25565`
```bash
java -jar CraftSocketProxy-1.1.3.jar --c -host example.com -port 80 -proxy 25565
```

> [!WARNING]
> If the supplied port for the client is 80 or 443, then it will automatically attempt to connect to the host through a WebSocket connection.

**Proxy Server (WebSockets)** `localhost:25565 -> ws://localhost:80`
```bash
java -jar CraftSocketProxy-1.1.3.jar --s -host localhost -port 25565 -proxy 80
```

> [!IMPORTANT]
> If you already have a server using the port 80 and 443, use a different port and reverse proxy it to a different path.
> **Example:** `ws://example.com:80/minecraft`

## Full list of commands:
```text
Arguments:
--c               | Start a Client Proxy
--s               | Start a Server Proxy
-host  <Hostname> | Hostname
-port  <Port>     | Port of Host
-proxy <Port>     | Output port of Proxy
-path  <Path>     | (Optional) Path of WebSocket connection
--wss             | Use secure WebSocket (wss://)
--version         | Query version
```

> [!WARNING]
> The path requires a `/` at the beginning. `-path /minecraft`

### Examples

I have made three [examples](https://github.com/sss-ryun/craftsocketproxy/tree/master/examples/src/main/kotlin/) if you
want to use this as a dependency and create your own plugin or mod or whatever.

# DISCLAIMER
```
THIS PROJECT IS NOT AFFILIATED WITH MOJANG SYNERGIES AB OR MICROSOFT CORPORATION IN ANY WAY OR FORM.
```

# LICENSE
```text
Copyright 2024 SSS Ryun, also known as "SSS_Ryun", "sss-ryun", or simply "Ryun".

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
