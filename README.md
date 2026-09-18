# Avada Kedavra addon

Forge 1.20.1 addon for Iron's Spells 'n Spellbooks 3.16.1.

## Build

1. Install Java 17.
2. Download the **Forge** jar for Iron's Spells 'n Spellbooks 3.16.1 for Minecraft 1.20.1.
3. Rename it to `irons_spellbooks-1.20.1-3.16.1.jar` and put it in `libs/`.
4. Run `./gradlew build` (Windows: `gradlew.bat build`).
5. The output jar is in `build/libs/`.

The addon registers `avada_kedavra:avada_kedavra`. In a world with cheats, obtain a scroll/spell through the Iron's Spells 'n Spellbooks commands or assign the spell to a spellbook using that mod's normal UI.

The effect is targeted at up to 50 blocks and applies 100/200 initial true damage, four ticks of 30/60 true damage at 0.25-second intervals, three lightning bolts, and Wither for 30 seconds if the target survives.
