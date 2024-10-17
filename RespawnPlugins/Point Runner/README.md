PointRunner
===========

Point Runner Mini Game

Config file
-----------

The config file is in the YAML format. Avoid using tab characters for indentation, use 4 spaces.
Below is an outline of the config parameters it supports:

**AutoStart:** Whether the game should auto starts if there are enough players (more than 2).

**AutoStartRequired:** The minimum amount of players to start a game. If a player leaves during a game auto starting, and the player count is less than this number then the auto start will abort.

**GameEndTimer:** The time (seconds) between the game ending, and either the server shutting down, or if BungeeCoordReturn is true then the point in time in which the players are sent the Bungee server command.

**BungeeCoordReturn:** Whether a BungeeCoord plugin channel message should be sent to all players, in order to send them to another server before shutdown.

**BungeeCoordDestination:** The name of the BungeeCoord identifier for the server in which players will be sent to.

**BungeeCoordGracePeriod:** The time (seconds) between players being sent the return command, and the server shutting down.

**StartMessageInterval:** The interval of the auto start message that tells you when the game is going to start.

**StartTimer:** The duration of the auto start timer (seconds).

**CanJump:** If the player can jump when in the arena.

**CanSprint:** If the player can sprint when in the arena.

**UseSpeedBoost:** If speed boost is enabled when in the arena.

**SpeedBoost:** Speed boost level (as per Minecraft potion effects).

**SpawnTimer:** The duration of the spawn timer (seconds). This is after you have been teleported into the arena, but before the game has started.

**DecayTime:** The time duration between the player standing on a block and it disappearing (game ticks).

**SpawnPosition:** The vector position of where players spawn and are teleported to when they die.

**SpawnPoints:** This is a list of vectors of arena spawn points for players. A random one is picked for each player, and if there is not enough then the player will not participate in the game.

**ArenaHeight:** The height (Y coordinate) of the top layer of blocks of the arena. This is used for spawn points.

**BlockTypes:** A list of block ids, and their associated block types. 0 is a normal point block, 1 is a death block and 2 is a multipler block.

**BlockMetaData:** A list of block ids, and their associated metadata. For blocks of type 0 this will be the blocks point value. For blocks of type 2, this will be the duration in ticks of the multiplier effect.

Commands
-----------

**startgame:** Forces game to start. Requires "pointrunner.startgame" permission.

**stopgame:** Forces game to stop. Requires "pointrunner.stopgame" permission.

**removeplayer:** Requires player name as argument. Removes player from game, so they are free to wonder around the map. Requires "pointrunner.removeplayer" permission.

**editmode:** Requires action as first argument. Actions are join, leave, save, reset and list. When in editmode, you can add/remove spawn point in game by left clicking on a block. Also has an optional secondary parameter for player (defaults to command sender otherwise).
