
----------
# Overview

SteamInspect is a utility library designed to allow for programmatic discovery and inspection of a user's Steam installation.

On Windows, the main Steam installation location is located empirically by inspecting the relevant registry key. The `jna` feature artifact provides an implementation that does this using [JNA](https://github.com/java-native-access/jna), but in it's absence the base artifact can use `REG QUERY` via shell commands.

Support for Linux is more limited, but discovery should still work if Steam is installed in a standard location.

Once SteamInspect discovers the User's primary Steam library, it can follow metadata to the rest of the Steam installation locations on the machine, enumerating all installed games, their installation directories, and their downloaded workshop items.

Steam stores very little locally in its own formats about workshop items. To get even the name of a mod, I suspect I'd have to individually support every game's custom mod file structure. In lieu of that nightmare, I have implemented a client for Steam's Web API. Most of the endpoints require some level of authentication, so we are currently only querying workshop details from the `ISteamRemoteStorage/GetPublishedFileDetails` method. The API offers responses in the same VDF/KeyValue format we're already handling for the local metadata, so we don't require a runtime dependency on a JSON parser.

The networking implementation now implements a simple cache, governed by the `expires` directives returned from the API.  

----------
# Alternatives

[steamapp.cmd](./steamapp.cmd) is a Windows command shell script which can retrieve a more limited set of information. It can query the registry for Steam's location, and it has a rudimentary VDF parser implemented (see the `:QUERY_VDF` subroutine) to parse the library and application manifests. It may be useful, either in its current form or as a jumping off point for your own script.