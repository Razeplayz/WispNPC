# WispNPC

Combat NPCs and a knockback training dummy for **Paper 1.21.11 / Java 21**.

> These NPCs are equipped vanilla zombie entities, not player-profile NPCs. The fighter uses vanilla zombie AI and targets players; it does not simulate advanced player PvP movement or sword combos.

## Commands
- `/npc spawn fighter` — spawn an aggressive, geared fighter.
- `/npc spawn netherite` — spawn a stationary dummy with enchanted netherite gear. It does not attack or chase and can be hit for knockback practice.
- `/npc remove` — remove the WispNPC you're looking at (within 6 blocks).
- `/npc reload` — reload `config.yml`.

Commands require `wispnpc.admin` (operators by default). NPCs are tagged in persistent data and configured to persist across chunk unloads/restarts.

## Build automatically
Open **Actions → Build WispNPC → Run workflow**. After a successful run (green check), open the run and download the `WispNPC-jar` artifact. Unzip it and upload the `.jar` inside to your server's `plugins/` folder, then restart.

Target: Paper 1.21.11, Java 21.