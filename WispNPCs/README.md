# WispNPCs 1.0.0

WispNPCs adds player-like NPCs through Citizens and lets you run commands when a real player right-clicks an NPC.

## Requirements
- Paper 1.21.11
- Java 21
- Citizens compatible with your server version

Citizens NPCs look like players, but they are not real connected clients. Some combat or item plugins require an actual online player and may ignore NPCs. This plugin cannot make every third-party plugin treat a fake player as a real client.

## Build with GitHub Actions
The repository workflow **Build WispNPCs** builds this folder automatically. Open **Actions**, select the workflow, run it with **Run workflow**, then open the successful run and download the `WispNPCs-jar` artifact. Extract the JAR and upload it to your server's `plugins/` folder. Install Citizens too, then fully restart the server.

## Commands
- `/wnpc create <name>` — create a player-like NPC at your location.
- `/wnpc list` — list Citizens NPCs and IDs.
- `/wnpc skin <id> <playername>` — request a skin.
- `/wnpc kit <id> <kit-name>` — right-click runs `/kit <kit-name>` as the clicking player.
- `/wnpc action <id> player <command...>` — run a command as the clicking player.
- `/wnpc action <id> console <command...>` — run a command as console.
- `/wnpc delete <id>` — delete an NPC.

Commands support `{player}`, `{npc}`, and `{npc_id}` placeholders. Do not include the leading slash in the command text.

Example:
`/wnpc create PvP Kit`
`/wnpc kit 1 pvp`

The kit command assumes your kit plugin accepts `/kit <name>`. If WispKits uses a different claim command, use `/wnpc action <id> player <command>`.
