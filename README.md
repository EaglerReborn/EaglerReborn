###BTW see credits.txt
![image](https://github.com/EaglerReborn/EaglerReborn/assets/134523836/e59bba1d-266b-4313-aa8f-18a7d39887b7)
---
### Eaglercraft client with Java support.

### This repository contains:

 - **Utilities to decompile Minecraft 1.8 and apply patch files to it**
 - **Source code to provide the LWJGL keyboard, mouse, and OpenGL APIs in a browser**
 - **Patch files to mod the Minecraft 1.8 source code to make it browser compatible**
 - **Browser-modified portions of Minecraft 1.8's open-source dependencies**

### This repository does NOT contain:

 - **Any portion of the decompiled Minecraft 1.8 source code or resources**
 - **Any portion of Mod Coder Pack and it's config files**
 - **Data that can be used alone to reconstruct portions of the game's source code**
 - **Software configured by default to allow users to play without owning a copy of Minecraft**

## Getting Started:

### To compile the latest version of the client, on Windows:

1. Make sure you have at least Java 11 installed and added to your PATH
2. Download (clone) this repository to your computer
3. Double click `CompileLatestClient.bat`, a GUI resembling a classic windows installer should open
4. Follow the steps shown to you in the new window to finish compiling

### To compile the latest version of the client, on Linux/macOS:

1. Make sure you have at least Java 11 installed
2. Download (clone) this repository to your computer
3. Open a terminal in the folder the repository was cloned to
4. Type `chmod +x CompileLatestClient.sh` and hit enter
5. Type `./CompileLatestClient.sh` and hit enter, a GUI resembling a classic windows installer should open
6. Follow the steps shown to you in the new window to finish compiling
7. (real!)


## Joining Non-Eagler servers


To join non eagler servers on the client, just do not include `wss://` in the server address. If you include `wss://`, the client will assume its an eagler server.

If the server has both Java and Eagler support, you should definitely use Eagler. Here's why:

1. Eagler is much faster than Java
2. Eagler is supported natively, with needing to use a relay, resulting in better ping.

---

### Setting up a relay


<a href="https://replit.com/new/github/EaglerReborn/relay"><img src="https://raw.githubusercontent.com/BinBashBanana/deploy-buttons/main/buttons/remade/replit.svg"></img></a>


Just click the "Run on replit" button to make a relay.
make sure to set it to "blank repl" 
run the repl, then wait a bit.
once u see the webview, ur done. (once u see the `>` its 100% finished) 
take the link in the webview and replace `https://` with `wss://`. then, fork this repo and go to `sources/main/java/me/otterdev/eaglerreborn/NetworkSettings.java`. take the `wss://` link u made earlier and replace `wss://reborn-relay.otterdev.repl.co` with your relay. Afterward, compile the client and your relay is all setup!
