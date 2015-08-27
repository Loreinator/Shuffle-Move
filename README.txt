Shuffle Move v0.3.23

~~ About ~~
A program to identify and display the best next move for the game Pokemon Shuffle. I do not own any part of Pokemon Shuffle, which is the property of Nintendo. All credit is given in the documentation within the source code. 

~~ Installation ~~
1) Update to at least Java 8
2) Launch Shuffle Move vX.X.X.jar
	Windows: Use Shuffle Move vX.X.X.exe
	Other: Use Shuffle Move vX.X.X.jar or from the command line navigate to this directory and enter:
		java -jar "<Jar name>" 

~~ Instructions ~~
1) Install
2) Launch program
3) Click Help -> Help to see the help documentation.
	For all other problems, see the release thread listed at https://www.reddit.com/r/ShuffleMove/wiki/versionlinks

~~ Planned Features ~~
Go to https://github.com/Loreinator/Shuffle-Move/issues

~~ Contact ~~
Email: all4atlantis@gmail.com
GitHub: https://github.com/Loreinator/Shuffle-Move
Reddit: https://www.reddit.com/r/ShuffleMove/wiki/index

~~ License ~~
Copyright 2015 Andrew Meyers
Splash screen and window icons: reddit.com/u/ArmpitWrestling
German Translations: reddit.com/u/ArmpitWrestling
Finnish Translations: reddit.com/u/I_get_in
Chinese Translations: reddit.com/u/Pingtendo
Species Icons: serebii.net

This program is licensed under GNU General Public License Version 3. Please read and agree to the terms of the included license before use.

~~ Changelog ~~
v0.3.23 - 2015-08-
	-
v0.3.22 - 2015-08-26
	- Bug fixes: Focus issue, last 3 abilities proc when there is 3 turns left, not from the 3rd turn.
	- Abilities corrected: Sky Blast, Double Normal
	- Save prompt now included if your data changes in any way, before you close.
	- Translations updated
	- Account for chain breaks due to thawing
v0.3.21 - 2015-08-23
	- Bug fixes
v0.3.20 - 2015-08-23
	- Metal blocks are now fully simulated - they expire after 5 turns, and disappear after moves are chosen and before they are simulated.
	- HP and Score visualization improvements
	- You can now fill the board with your selected Paint by pressing Ctrl-F or clicking the item in the Board menu
	- The roster panel can now be filtered by team.
	- Effects can now be told to only occur if they at least meet a threshold for likelihood (non-random check). 
		- A threshold of 0 will allow all effects, and a threshold of 100 will only allow 'without fail' effects. 
		- If the odds equal the threshold, the result is otherwise undefined.
v0.3.19 - 2015-08-22
	- Fix for settle taking a move away
	- Team data checking and usage improved
	- Finnish translations updated
	- Effects can now be forced off via the Move Preferences window
	- Off by one correction to combo multiplier query
	- German translations updated
	- Stage moves remaining can now be increased by up to 5 higher than the max for that stage
	- Rounding error corrected for scores when using the 1.15 chain multiplier (core float adjustment)
	- Attack Power Up can now be toggled on and off in the paint pallet. This effectively doubles the base power of all species in the simulation.
v0.3.18 - 2015-08-17
	- Bugfix for spellfixes not being adopted when upgrading
	- Spellfixes for Vivillon and Terrakion
v0.3.17 - 2015-08-17
	- Visual bug with paint pallet not showing the scroll bar or allowing scrolling has been fixed.
	- Health and Moves remaining is now modeled for all main stages and select special stages. 
	- Effects now simulated properly: Poisonous Mist, Downpour, Swarm, Steely Resolve, Vitality Drain, and Last Ditch Effort.
	- Species updated for entries 2820-3030
	- Stages added: 211-220, EX_25-EX_27, SP_303 (Darkrai)
	- Stage updated: SP_003M (Mega Venusaur with move-based competition)
v0.3.16 - 2015-08-14
	- Updater will now tell you where the new *.zip is located.
	- Simulation Accuracy improvements
	- Effects with ++ in their name are properly displayed on the team and roster editor bottom label.
	- Preferences default number of simulations per move is now 5 (up from 1) to better reflect randomness.
	- None and Wood are no longer listed in the stages selector
	- Freezing delay is now included in the simulation (1 additional frame before releasing a block)
	- Mega Garchomp's Effect is included in both configuration and the simulation
v0.3.15 - 2015-08-08
	- Fixed a serious bug that was present whenever you had no mega slot selected.
	- Improved the build task script.
v0.3.14 - 2015-08-06
	- Changed working directory to user.home for all platforms
	- Updated species and stages for the new content
	- Now simulating Dragon Talon and Heavy Hitter appropriately
	- Species configs will now update if they are out of date, automatically.
	- Improved active mega handling for frozen tiles
	- Improved the Migration service, it will temporarily remember where you were looking.
v0.3.13 - 2015-07-29
	- Blaziken's mega is now included
	- Bug fixes
	- Finnish translations updated
v0.3.12 - 2015-07-23
	- Species updated for new patch: Cresselia
	- Including new stages: Cresselia, Blaziken, Wobbufet
	- New Effect added: BARRIER_BASH_P (Barrier Bash+)
	- Barrier bash effect corrected
	- Fixed a bug with the freezing toggle for paints
	- Fixed a bug with effects not properly triggering in the simulation
	- Improved the occurrence of all effects according to http://pastebin.com/5uvZBN8S
	- Place-holders for most remaining effects added, with the odds mentioned above.
	- Crowd Control is now much more accurately scored, thanks to the research of /u/JustAnotherRandomLad
	- Fixed an issue where an initial mega combo sometimes allowed a normal ability to also activate
	- Fixed the attack bonus for AP 30 species as they level
v0.3.11 - 2015-07-14
	- Menu i18n keys updated
v0.3.10 - 2015-07-12
	- Sky Blast added for Braviary
	- Unfreezing is now registered as a disruption again, in every way that an unfreeze action can occur.
	- New Grading mode: Rank by Coordinate
	- Updated the built-in preferences.txt to include the CELL_BORDER_THICK_OUTER key (defines the from & to border thickness).
	- Fixed Manectric's ability thanks to /u/screw_dog
	- Fixed Ampharos's ability thanks to /u/screw_dog
	- Fixed a bug with prospective combo traversal - now using a TreeSet instead of a PriorityQueue
v0.3.9 - 2015-07-09
	- Chinese translations added
	- Fonts for interface elements will now use the java default font, but inherit the size and style as defined in your configurations
	- Some display bugs fixed
	- Separated line thickness for inner and outer cell borders
	- Fixed the fine point about mega progress versus frozen states. The mega increase will only increase for comboed unfrozen blocks now.
	- Updated species and stages to include the new content
	- Mega Manectric and Mega Heracross's abilities are now included
	- Moves can be ranked by Mega Progress
	- The Move Chooser information is much more detailed, including (if necessary) the range and average instead of just a truncated average.
v0.3.8 - 2015-06-22
	- Corrected Spiritomb's attack power
	- Including missing feature from v0.3.5 (delete in express mode)
	- Updated species list to include new species (Manaphy, phione, etc.)
v0.3.7 - 2015-06-15
	- Fixed bug with Dialga's ability
v0.3.6 - 2015-06-15
	- Corrected Finnish translations
	- Added stages for SP_Dialga, SP_Giratina, SP_Blastoise
	- Added stages for ex22-24
	- Added Dialga
	- Added BLOCK_SMASH_P for Block Smash+ as an available effect.
	- Windows Executable added, requires the jar to be in the same location as itself though.
v0.3.5 - 2015-06-08
	- Bug fix for a network issue.
	- Included i18n for Finland
	- Main window is now resizable
	- Bug fix for the missing border on the selected paint for teams
	- Pressing delete in express mode will now erase cells and advance the cursor
	- Mega, Frozen, Coin, Metal, and Wood buttons and selectors are now included in the main interface.
	- New species added: Giratina, stages 190-200.
	- Effect rates updated
	- Mega speedups included in roster editor, and accounted for in the simulation and interfaces
	- Some responsiveness gains in the interface at the expense of simulation expediency (decreased thread priority to MIN_PRIORITY).
	- Migration tool now works with legacy and current configurations.
		- If the file ends with teams.txt or roster.txt it will be treated like the pre-v0.3.2 data files
		- Otherwise, it is treated like the current teamsData.txt and rosterData.txt data files
v0.3.4 - 2015-05-31
	- Bug fix for score issue when you have more than one feeder on a stage with coins.
v0.3.3 - 2015-05-30
	- Shaymin added, Victini added
	- All icons added, with their mappings. 
		- See config/defaults/icons.txt for the names to use for new species as they are added to the game.
	- Bugfixes for Linux & Mac
	- Update service interface rework and optimizations for a better user experience
	- German Translations added
	- Language selection added to Help menu
	- Program can now load remotely via command line
	- Roster and Teams editors gain new filter options: Mega (yes or any), and by effect (selected only, or any).
	- New Team functionality - retains current team if the new stage doesn't have a non-empty team.
	- Program is now packaged into a single jar for all required resources and functions fine in any OS or file system. 
	- Performance optimizations for all image loading
	- No longer saves empty files
	- Board defaults are now included inside the jar
	- i18n is now open for any new language
	- Release jar is now signed automatically
	- Configurable size of teams and roster editor - saves position whenever you resize them and hit file-> save
	- Lucario, Lopunny, and Kangaskhan bug fix for score and effect
	- Ampharos bug fix for pattern of effect
	- Simulation bug fix for score combo multipliers
	- Effects now include:
		- All megas, Power of 4, Power of 5, Opportunist,
		- Block Bash, Pummel, Burn, Pixie Power, Freeze,
		- Pyre, Rock Break, Barrier Bash, Dancing Dragons,
		- Sinister Power, Quake, Crowd Control, Counterattack,
		- Hitting Streak, Damage Streak, Swat, Brute Force,
		- Spookify, Stabilize, Stabilize+, Quirky, Quirky+
	- Improved combo multiplier handling
	- Improved image rendering, Bicubic image interpolation will be used if your renderer allows it (smoother icons)
	- Mega progress tracking is now included, selectable via the teams editor and the team menu. 
		- Values taken from https://www.reddit.com/r/PokemonShuffle/comments/37ny6g/number_of_matches_for_each_mega_evolution/crod2qf
v0.3.2 - 2015-05-24
	- Config Framework rework
	- I18n framework and base now included (translations welcome)
	- Move chooser dialog
	- Migration service
	- Many interface components are customizable though configurations.
	- Bug fixes for mega lucario, etc.
	- Settle feature returns
	- fix bug with the order of keybinds in the team editor
	- added rock break and block bash
v0.3.1 - 2015-05-19
	- Fixed Null Pointer Exception for Mega Mewtwo Y
v0.3.0 - 2015-05-18
	- Complete rework, very little of v0.2.5 remains
	- Teams and Roster editing included
	- Smoother user experience overall
	- Update checker improvements
	- Simulation redesign to account for 1/120ths of a second resolution
	- All megas re-implemented to completely account for their actual effects
	- Feeders added for the simulation (randomly generates blocks to fall into the board)
	- Multithreading support for simulation (multicore cpus will experience a performance advantge for the simulation)
	- Teams are stage based
	- In-program help and about documentation
	- etc... the list goes on
v0.2.5 - 2015-04-20
	- Bug fix for renaming blocks with upper case letters to "M", then to something else
v0.2.4 - 2015-04-18
	- now includes Mega Aerodactyl. The only unsupported mega is ampharos now.
	- auto update checking now implemented
	- metal block hard-coded
	- special blocks no longer appear on the block config panel
	- air blocks do not appear in the paint pallet anymore, but are still fully functional
v0.2.3
	- compatability fix block rows
v0.2.2
	- bug fixes for frozen handling, etc.
	- save/load by type feature added
v0.2 - 2015-04-15 
	- GUI release 
	- Many features added
	- Program is now under GNU GPLv3
v0.1 - 2015-04-12 Initial release
