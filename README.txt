BETTER BARROWS

WELCOME

Better Barrows is a RuneLite plugin for Old School RuneScape.

It is made to make Barrows easier to understand.

If you have never written code before, that is completely fine. This documentation tries to explain everything in normal English instead of assuming you already know Java or RuneLite development.

Better Barrows does not play the game for you.

It does not click things, move your character, attack monsters, solve puzzles automatically, or open the reward chest for you.

Instead, it watches information that RuneLite can already see and shows that information in a clearer way.


ABOUT ME

Hi, I'm the developer of Better Barrows.

I would describe myself as a junior developer and a junior vibe coder.

That means I am still learning programming while building the project.

I have learned by testing things, breaking things, fixing things, reading open-source RuneLite code, looking at other Barrows plugins, reading documentation, and using AI to help me understand and develop the project.

I want to be open about that.

Better Barrows was not created perfectly on the first try.

Some features have been rewritten several times because testing them inside RuneLite showed problems that were not obvious from simply reading the code.

That is normal software development.

One of my goals with this project is to leave behind documentation that another beginner can actually understand.


WHAT IS RUNELITE?

RuneLite is an open-source client for Old School RuneScape.

RuneLite supports plugins.

A plugin is a small piece of software that runs inside RuneLite and adds extra information or useful features.

Better Barrows is one of those plugins.


WHAT DOES BETTER BARROWS DO?

Better Barrows focuses on the Barrows activity in Old School RuneScape.

It helps with several parts of a Barrows run.


SURFACE MOUNDS

There are six Barrows brothers:

Dharok
Ahrim
Verac
Torag
Karil
Guthan

Each brother has a mound on the surface.

Better Barrows can highlight the full area of each mound and show the brother's name.

The colours tell you useful information.

Red means the brother is still alive.

Green means the brother has been killed.

Blue means that crypt is the tunnel entrance.

Blue is given priority because knowing where the tunnel is can still be useful even after that brother has been killed.


MINIMAP LETTERS

Better Barrows can also show the brothers on the minimap.

D means Dharok.
A means Ahrim.
V means Verac.
T means Torag.
K means Karil.
G means Guthan.

These letters use the same basic brother information as the large mound highlights.

This is important because we do not want two different parts of the plugin disagreeing about where a mound is.


FINDING THE TUNNEL

One of the Barrows crypts contains the tunnel entrance.

When you search a sarcophagus, Better Barrows remembers which brother's sarcophagus you just searched.

It then waits to see what happens.

If that brother appears, the plugin knows this was a normal brother encounter.

If the tunnel interface appears, the plugin knows this is the tunnel entrance.

There is also a short five-second backup check.

This exists because testing showed that relying on only one message from the game was not always reliable.

For example, an older version trusted the message:

"You don't find anything."

too much.

That could cause the plugin to mark the wrong mound as the tunnel.

The newer system waits for better evidence instead.


REMEMBERING THE TUNNEL

Once Better Barrows knows which crypt contains the tunnel, it can remember and display that information.

This is useful because you might leave the crypt and later need to remember where the tunnel was.


TUNNEL MONSTER KILL TRACKING

Better Barrows can count certain monsters that you kill inside the tunnels.

These include:

Bloodworm
Crypt rat
Giant crypt rat
Crypt spider
Giant crypt spider
Skeleton

You can choose a target number for the monsters you care about.

For example, if your Skeleton target is 2, Better Barrows can count until you have killed two Skeletons.


MONSTER OUTLINES

Better Barrows can highlight tunnel monsters that you still need for your chosen targets.

The plugin uses RuneLite's model outline system.

In simple terms, this draws an outline around the visible shape of the monster.

When you have reached the target for that monster type, Better Barrows no longer needs to highlight it as a required target.


TUNNEL DOORS

Better Barrows can highlight useful doors inside the Barrows tunnels.

This helps make the maze easier to read.

The plugin does not click the door.

It only makes the door easier for you to see.


PUZZLE HELP

The Barrows tunnel can show a puzzle.

Better Barrows can highlight the answer you should choose.

It does not click the answer for you.


REWARD POTENTIAL

Barrows has a reward-potential value.

Better Barrows can show this information so it is easier to keep track of during a run.


CHEST VALUE

When you loot the Barrows reward chest, Better Barrows can calculate the total value of the items you just received.

It is careful not to count items that were already in your inventory.

For example:

You already have 1,000 death runes.

The chest gives you 200 more.

You now have 1,200 death runes.

Better Barrows should value the 200 new runes, not all 1,200.

To do this, the plugin remembers what was in your inventory before the loot arrives and compares it with what is there afterwards.

It then adds together the value of the new items.

The result can look like:

Better Barrows: Total chest value: 184,532 gp


WHAT BETTER BARROWS DOES NOT DO

Better Barrows is designed to give information to the player.

It does not automatically:

Move your character.
Attack a brother.
Attack a tunnel monster.
Search a sarcophagus.
Dig on a mound.
Open a tunnel door.
Choose a puzzle answer.
Loot the chest.

You still make the decisions and perform the game actions yourself.


WHY IS THE PROJECT OPEN SOURCE?

Open source means people can look at the source code.

That is extremely important to this project.

I learned a lot by being able to look at other open-source RuneLite projects.

I want Better Barrows to be understandable in the same way.

People should be able to see what it does, report mistakes, suggest improvements, and learn from it.


BIG THANK YOU TO THE PROJECTS THAT INSPIRED BETTER BARROWS

Better Barrows was strongly inspired and guided by existing RuneLite and Barrows projects.

RuneLite and RuneLite Barrows Brothers:

https://github.com/runelite/runelite

https://github.com/runelite/runelite/wiki/Barrows-Brothers

Barrows Door Highlighter:

https://github.com/hansjm10/Barrows-Door-Highlighter

Barrows Sarcophagus Memory:

https://runelite.net/plugin-hub/show/barrows-sarcophagus-memory

https://github.com/vahnx/barrows-sarcophagus-memory

Barrows Tunnels Kill Tracker:

https://runelite.net/plugin-hub/show/barrows-tunnels-kill-tracker

https://github.com/vahnx/barrows-tunnels-kill-tracker

These projects helped me understand what was already possible and strongly guided the ideas and thought process behind Better Barrows.

Some were sources of ideas.

Some were useful examples of how RuneLite problems could be approached.

Where actual source code is copied or adapted, its licence must also be respected.

THIRD_PARTY_NOTICES.txt explains this in more detail.


FINAL WORD

I am still learning.

There will probably be things experienced developers would write differently.

That is okay.

I would rather be open about the learning process and improve the project over time.

If you are also a beginner and you opened this project because you want to understand how a RuneLite plugin works, welcome.

ARCHITECTURE.txt explains the code in beginner-friendly language.

CONTRIBUTING.txt explains how you can help.

THIRD_PARTY_NOTICES.txt explains the projects that inspired Better Barrows and why software licences matter.
