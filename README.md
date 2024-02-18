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
