# Barrows with Benefits

Barrows with Benefits is a RuneLite plugin that brings a collection of useful Barrows helpers together in one place.

The idea is to make a Barrows run easier to follow from start to finish. It keeps track of the brothers you've killed, remembers where your tunnel is, helps while you're moving through the tunnels, and gives you some useful information about your rewards at the end.

Everything can be configured through RuneLite, so you can use the parts you find useful and turn off the ones you don't.

## At a Glance

- **Mound Highlights** — Highlights and labels all six Barrows mounds, with colours that update as your run progresses.
- **Brother Tracking** — Tracks which of the six brothers you've defeated during the current run.
- **Minimap Markers** — Adds simple brother markers to the minimap using the same live status as the mound highlights.
- **Tunnel Detection & Memory** — Detects which crypt leads into the tunnels and remembers it for the rest of the run.
- **Tunnel Kill Tracker** — Counts supported NPC kills inside the Barrows tunnels against configurable targets.
- **NPC Outlines** — Uses RuneLite's model-outline system to highlight tunnel monsters you still need.
- **Tunnel Door Highlights** — Highlights usable Barrows doors to make navigating the tunnels a little easier.
- **Puzzle Helper** — Identifies and highlights the correct answer to the Barrows door puzzle.
- **Reward Potential** — Displays your current Barrows reward potential while you're underground.
- **Chest Value** — Detects newly received chest loot and gives you an approximate total value at the end.

---

## The Six Brothers

Barrows with Benefits keeps track of Dharok, Ahrim, Verac, Torag, Karil and Guthan throughout your run.

On the surface, each brother's full diggable mound area can be highlighted and labelled. Rather than drawing a grid of individual tiles, the plugin treats the mound as one area so the overlay stays clean and easy to read.

The colour tells you what's currently happening:

- **Red** — the brother is still alive.
- **Green** — you've defeated the brother.
- **Blue** — this crypt contains your tunnel entrance.

If a brother has been killed but their crypt also contains the tunnel, the tunnel state takes priority and the mound remains blue. That way the most useful information isn't lost once the brother has been defeated.

### Minimap Markers

The same brother information is also used to place markers on the minimap:

**D** Dharok · **A** Ahrim · **V** Verac · **T** Torag · **K** Karil · **G** Guthan

The minimap isn't maintaining a second set of brother states. It uses the same underlying information as the surface overlay, so the two should stay in sync as the run changes.

---

## Finding and Remembering the Tunnel

Tunnel detection ended up being one of the more interesting parts of the plugin.

When you search a sarcophagus, Barrows with Benefits remembers which brother you just searched and waits to see what happens next.

If that brother spawns, it knows you've found the normal brother encounter.

If the tunnel interaction appears instead, it knows that crypt contains the tunnel.

There is also a short fallback check to handle situations where the expected game information doesn't arrive in quite the way the plugin expects.

This is deliberately a little more careful than simply reacting to one chat message. Earlier versions did that and could incorrectly mark a crypt as the tunnel even when the brother was about to spawn.

Once the tunnel has been confirmed, Barrows with Benefits remembers it and updates the relevant surface and minimap markers.

---

## Inside the Tunnels

Once you're underground, the plugin switches its attention towards navigation and getting the kills you want.

### Kill Tracker

Barrows with Benefits can track:

- Bloodworms
- Crypt rats
- Giant crypt rats
- Crypt spiders
- Giant crypt spiders
- Skeletons

You can configure targets for the monsters you care about.

When one of those NPCs dies, the plugin checks the NPC itself to make sure it was actually inside the Barrows tunnels before adding it to your count.

It also remembers NPC deaths it has already processed. This prevents one monster from accidentally being counted twice if RuneLite happens to expose the same death to the plugin more than once.

### NPC Outlines

The kill tracker also ties into the NPC highlighting.

Barrows with Benefits uses RuneLite's `ModelOutlineRenderer` to draw an outline around tunnel monsters that are still useful for your configured targets.

For example, if you've asked for two Skeleton kills and you're currently at one, Skeletons can remain highlighted.

Once you reach two out of two, the target is complete and the plugin no longer needs to highlight Skeletons for that target.

This keeps the highlighting tied to what you actually still need rather than permanently outlining every supported NPC.

---

## Tunnel Doors

Barrows with Benefits can highlight usable doors inside the Barrows tunnels.

The plugin listens for the relevant wall objects appearing and disappearing from the game scene and keeps track of the Barrows doors that are currently available.

Those objects can then be highlighted by the overlay, making the maze a little easier to read at a glance.

Nothing is opened automatically — the plugin is only giving you a clearer visual target.

---

## Puzzle Helper

When you reach one of the puzzle doors, Barrows with Benefits can identify the correct answer and highlight it.

This is only a visual helper. The plugin doesn't automatically interact with the puzzle or click the answer for you.

You still make the actual interaction yourself.

---

## Reward Potential

Barrows with Benefits can display the game's current Barrows reward potential while you're moving through the tunnels.

This gives you a quick way of checking your progress without having to keep track of it yourself while deciding whether to continue killing tunnel monsters or head towards the chest.

---

## Chest Value

At the end of the run, Barrows with Benefits can calculate the approximate value of the loot you actually received from the chest.

This isn't done by simply valuing everything in your inventory.

Instead, the plugin takes a snapshot of your inventory around the chest interaction and compares the quantities when the inventory changes.

For example, if you already had **1,000 Death runes** and your chest gives you another **200**, the plugin should value the **200 new runes**, not the full stack of 1,200.

The newly received items are priced and combined into one total, which is then shown as a single message after looting.

---

## Configuration

Most of Barrows with Benefits can be adjusted through RuneLite's normal plugin configuration panel.

You can choose which helpers you want to use, set tunnel kill targets, and customise supported colours and display options.

The goal isn't to force one particular way of doing Barrows. If you only want a couple of the helpers, you can simply disable the rest.

---

## Credits

Barrows with Benefits wouldn't have developed into what it is without the RuneLite community and the open-source Barrows plugins that came before it.

A big thank you to:

- **RuneLite & RuneLite Barrows Brothers**
- **Barrows Door Highlighter** by hansjm10
- **Barrows Sarcophagus Memory** by vahnx
- **Barrows Tunnels Kill Tracker** by vahnx

These projects have been useful references and a major source of inspiration while learning how RuneLite plugins work and thinking through how the different parts of Barrows with Benefits should behave.

Where third-party source has been used or adapted, the original project's licence and copyright still apply.

More detailed attribution and licence information can be found in `THIRD_PARTY_NOTICES.txt`.

---

## Licence

Barrows with Benefits is open-source software released under the **BSD 2-Clause License**.

**Copyright © 2026 SquidMuncher420**

See [`LICENSE`](LICENSE) for the full licence.
