<div align="center">
<h1>EasyMissions – Missions / Scrolls Plugin</h1> 

> **NOTE:** This plugin is still in **alpha**. Expect instability and possibly bugs and breaking changes between versions.

#### A PaperMC missions plugin aiming to use the latest Paper APIs while staying configurable, supporting FoliaMC (Unstable), performant (I wish), and easy to work with.
</div>

## Table of Contents
- [Important Notes](#some-things-you-need-to-know-please-read)
- [Features](#the-beginning-features)
    - [Mission Types](#mission-types)
    - [Anti Exploit](#anti-exploit)
    - [Wildcard Targets](#wildcard-matching-for-targets)
    - [Mission Defaults](#mission-defaults)
    - [API](#api)
- [Commands](#commands)
- [Config Overview](#config-overview)
    - [Main Config](#configyml--main-config)
    - [Mission Config](#missionsany-yml-file--mission-config)
    - [Defaults](#missionsdefaultyml--mission-defaultsdefault-config)
- [Placeholders](#placeholders)


## Some things you need to know, Please read.
To begin:

Due to the current stability state of EasyMissions which is **alpha**, there will be 0 guarantees about stability from one version to another in your server, right now it should be fulfilling enough to just work out of the box for you and all types and features should be working correctly as intended. However, that doesn't assure that breaking changes that may cause things like older mission items being entirely stale or useless, different logic applying for features or types, and architecture changes will not occur.

Breaking changes will be noted in each release, once it is stable you can expect this section in the README to be removed

Now for the important details about how this works:

When a mission item is created, it's **linked to the config entry it belongs to**.

This idea has both good and bad. On one hand, it's insanely easy to *brick* **ALL** missions belonging to the config entry if the config entry is missing or broken. On the other hand, this design allows for control over all missions and makes it possible to go back and fix your mistakes or push updates globally.

And so here comes the benefits:

Because missions always reference their config entry:
- Changes made in the configuration are **applied globally**.
- New changes (such as new textures or targets) can be added simply by updating the config.
- Existing missions automatically get those changes the next time a player progresses them.

For example, if you decide to have a new texture for missions on your server, players will see it immediately without needing to get new missions. Likewise, if a mission was created with the wrong target or type, correcting the config fixes the issue everywhere.

This gives server admins **full control over all missions**, but also puts the responsibility on them to maintain valid configurations to not brick everything up.

In attempt to mitigate how easy it is to brick things with this system, EasyMissions is **strict**:
- Mission configs will be **FURIOUS** if you misconfigure them.
- If a mission config is missing certain options or is invalid, **ALL configuration loading for the plugin is stopped**.
- This forces issues to be fixed immediately rather than allowing the server to run in a broken state.

Due to the goals of EasyMissions, there is **NO** older version support, no spigot/bukkit support and no guarantee that newer api usage as updates happen won't break it on the versions it already supported. As of now EasyMissions works only on:

- PaperMC and probably most of its derivatives/forks
- FoliaMC (Considered unstable, race conditions might exist, and I'm not familiar with folia that much, but I can tell you it starts and I didn't run into any issues so far)
- At least 1.21.5 Minecraft version (This is according to the PaperMC diff, the minimum version I tested it on was FoliaMC 1.21.8)
-----

#### Now that we've gone through the base and if you are still interested, let's take a look at what this thing really offers and how to use it, You can always take a look at the wiki (If I've made it by this time) to get the most detail as this is just a quick overview that does its best to explain most of the things

# The beginning: Features
I've tried to make the plugin offer some good things to make things fun and cool. so here they are:

#### Mission Types
---
Types are the most important in the plugin and the second most important part of a mission item's identity after the config entry it belongs to. they specify what targets you may use and what the player needs to do to progress the mission.

There is **24** available mission types ranging from things as simple as "kill 3 sheep" to specific ones that allow you to do things like "enchant a netherite sword with sharpness level 5", you may see all types later down there and their detailed descriptions and examples in the wiki

Thanks to the MissionType implementation (which may not be the cleanest) it's possible to do a lot of things with EasyMissions and to make use of that I made:
**Complex Types!**
Complex types allow you to have more than 1 condition/target for something, by default you'll notice that normal types focus on only one target, like "enchant diamond sword" but what if you want to also specify what enchant? what level? that's where complex types come in! these allow you to have more than one condition/target for the mission to progress, as of the time of writing this there is only one complex type, and it is ***complex_enchant*** which allows you to have targets containing the enchant, its level and the item and only when a player matches all those conditions the mission will progress.

An example is:

```yaml
  type: "complex_enchant" # this is the only complex type available. In the future there will be an addon to add many MANY more types including complex ones.

  targets:
    - SHARPNESS 5 DIAMOND_SWORD
```

as you can see in this snippet, we have each target separated by a space, the enchant, its level, and the item and now only when a player puts sharpness 5 on a diamond sword will the mission progress. The ordering of each target matters and must be preserved.

complex type implementations can have an infinite amount of conditions and if you would like to make your own complex type or just a normal type, check out the API documentation.

#### Anti Exploit
---
There are mission types that are really, really easy to exploit one of them is walk missions, you can just walk back and forth, and you will finish the mission in no time, or break missions where you can just place the block, break it, repeat instead of actually looking for that block.

Right now there are 2 available mitigations for this:

- **The recent place cache**  a very configurable cache that lets you set its size and its clean up frequency, when a player breaks/harvests a crop or block they placed intentionally in an attempt to cheat the system, the cache will realize that the block was recently placed by a player and will not count it as progress

- **The recent step cache** another configurable cache that lets you set its size per player, when a player walks back and forth or in a circle (depends on how big you configure the cache to be) the cache will realize they're not going anywhere and not count as progress

Some types will **NOT** have anti exploit features such as:

- **place** The tracking and association of a mission to a block break and then doing something like decrementing it seems complicated to me and I don't know how it would be reliably and efficiently implemented. The issue is that we are trying to revert progress instead of just checking before adding it like the existing solutions, I'm open to solutions and suggestions on how it could be done if you have a new idea.

- **craft** It's possible to cheat the system if you had a mission that required crafting 7 iron blocks, the player could do it with only 9 iron ingots and just craft them into block and ingot back and forth. it's complicated for me to figure out a way to actually make this without doing dirty things like tagging items or expensive tracking/heuristic, it's best for you to use one way recipes in your targets like pickaxes. I'm open to solutions and suggestions on how this could be done if you have a new idea too

Keep in mind that the following types may have exploits and could use more testing:
- **disenchant** Uses inventory events to get whether you disenchanted the item or not
- **enchant (While using Anvil)** Uses Inventory events to get whether you enchanted the item or not
- **complex_enchant (While using Anvil)** Uses Inventory events to get whether you enchanted the item or not
- **repair** Uses Inventory events to get whether you repaired the item or not

Right now I'm not aware of any other ways to cheat the system available other than the ones already fixed, if you find any please make an issue and if you have a new idea, make an issue with the suggestion tag too!

#### WildCard matching for targets
---
Imagine you want to make a mission that wants the player to mine 90 blocks of ANY ore, you would go and type out every. single. ore. that exists in the game, this is neither optimal nor is it fun to do and for that reason there is wildcard matching in the targets, now instead of typing every ore out you just do:

```yaml
type: break

targets:
  - "*_ORE"
```
this will increment for any block ending with ore, so diamond ore, iron ore etc. and their deepslate variants

The existing limitations are:
- Wildcard matching is basic for now, you can do things like \*\_ore or diamond\_\* or just \* or \*diamond\* for contains, more complex or specific wildcards that contain more than two *'s will not work and ? doesn't work either

#### Mission Defaults
---
Because the config and creation of a mission config can be a lot of work and typing, sometimes you just need to use one default for things such as the mission material, itemrarity, lore and name and their completed variants, and the category of the mission

That is where the **default.yml** comes in, options that aren't in the mission config will use the value from the default.yml so you don't have to specify everything even if it's always the same across all missions.

Now there are some options and things that just cannot be defaulted to, such as the type or the targets, the item model and its completed variant, the blacklisted worlds and the rewards.

#### API
---
Although right now the EasyMissions API lacks testing, it should be relatively easy to register new types and do other cool stuff with the API, It's as simple as getting an instance of the API with
```java
EasyMissionsAPI.getInstance()
```

You can take a look at the API documentation or the javadocs to see what you can do, but for now you can register a new mission type by doing this:

```java
EasyMissionsAPI api = EasyMissionsAPI.getInstance();

api.registerType(MissionType.matchEnum("cool_mission_type", CoolEnum.class));
```

> NOTE: Some methods in the API require you to only call before/after the server has loaded, in this case you cannot register a type after the server has loaded or else an exception will be thrown, for this reason it is recommended to register types in your onEnable() method, likewise some methods require you to run after the server has loaded like in an event listener or on ServerLoadEvent, you may safely assume that any method that interacts with a MissionConfig cannot be called before the server has loaded

MissionType has many convenience match methods like:
- **matchEnum** Match against an enum like Material.class
- **matchRegistry** Match against a registry like Enchantment
- **complex** Used to create complex mission types, this doesn't take classes but instead Validators which you can learn more about in the javadocs or API documentation

You can obviously have your own classes implementing the MissionType interface and TargetedMissionType and even your own Validators that implement the TargetValidator interface as some TargetedMissionType's use Validators under the hood

Now for events, right now there are only 2 events which are:
- **MissionClaimEvent** Fires whenever a mission is claimed, can be cancelled to prevent it from being claimed
- **MissionProgressEvent** Fires whenever a mission progresses, this can fire a lot and most likely will so be careful with what you do on it, it is cancellable and allows you to set the progress too

#### Ability to organize missions
----
Any mission config in a valid .yml file in the missions directory will be loaded by the plugin, this allows you to categorize missions and not deal with the burden of having one huge missions file that contains everything, instead you can split the files by mission type, targets, category or however you really want as long as it is a valid mission config in a .yml file in the missions directory

#### Custom textures
---
You can set custom textures for your mission items easily by using the **item_model** and **completed_item_model** options in the mission config, you will use your resourcepack's namespace followed by the texture you wish to use, you can also use existing Minecraft textures like minecraft:stone to use the minecraft stone texture, it is recommended that you use **item_model** instead of changing the **item_material** option, changes to those will also be applied on all existing missions with that config entry for everyone on the next time they progress

An example of how to use them is this:

```yaml
item_model: minecraft:stone # This will set the item's texture to stone regardless of its base item, it will have the behaviour of its base item but with a different texture

completed_item_model: minecraft:diamond_block # This will set the item's texture to a diamond block when it is completed
```

**item_model** defaults to the base item's texture when it is empty/unset

**completed_item_model** defaults to **item_model** if empty/unset which will also default to the base item's texture if empty/unset


#### Configurability
---
EasyMissions started with hardcoded types in an enum and the items were barely configurable, the way types were made back then made them only work with enum types, that made enchanting or more complex missions entirely dependent on the listener and gave little power to the creation of new types and the API, this would have ended up in very little types existing and the config lacking many options however this changed and now everything is easier for both the admin and the developers working on the plugin or API, you can configure almost every part of the item's display visible to the players such as:
- **Name and its completed variant** The display name for the mission depending on whether it's completed or not
- **Lore and its completed variant** The lore for the mission depending on whether it;s completed or not
- **Texture and its completed variant** The mission item's texture
- **Requirement min/max** You can set a range for mission requirements to generate between using the **requirement_min** and **requirement_max** options, if max is removed the mission will always be min
- **Blacklist worlds** Allows you to blacklist worlds to prevent missions from progressing in them, you can blacklist as many as you want

And other options, those were just the ones I believe are worth mentioning. Feel free to check out the example.yml
# Commands
For the normal players there is no available commands and any command requires easymissions.admin or OP to be executed

> **|** means OR in the examples, so [progress **|** requirement] means that both progress OR requirement are valid arguments

The list of commands available and what they do is:
- **/easymissions random [player]** gives a player a random mission (picks a category by weight, then a mission config belonging to that category)
- **/easymissions category-random [category] [player]** gives a player a random mission from a given category.
- **/easymissions give [player] [configentry]** gives a player a mission linked to the given config entry
- **/easymissions reload** reloads the plugin, some things such as turning a cache on/off require a restart and will not update by just reloading
- **/easymissions data** a little configurable chat message that displays info about the held mission and suggests commands to change them.
- **/easymissions set [progress | requirement | completed | entry]** allows you to change the data on the held mission to your liking, entry for example will let you use a different config entry for the mission if you want to migrate from an entry to another.
- **/easymissions list-types** gives you a list of all available mission types you can use

There are 2 aliases available and they are:
- **em** short for easymissions
- **easym** short but not so short for easymissions


# Config Overview
This will explain the main config, the mission config, and the default.yml config

#### config.yml | Main config
This is the main config of the plugin and contains the options related to the plugin itself and some global options for missions, such as the anti exploit features, timeout frequency, claim sound and messages

```yaml
# EasyMissions Configuration,
# Uses MiniMessage for coloring and placeholders.
# Different messages have different placeholders available, see the wiki for full lists.

debug: false

messages:
  reload: "<green>Reloaded successfully</green>"
  reload_fail: "<red>An error occurred while reloading, please check the console."
  needs_player: "<red>Only players can use this command.</red>"
  needs_mission: "<red>You must be holding a mission with a valid config entry to use this command</red>"
  give_mission: "<green>Successfully gave <mission> to <target></green>"
  rand_mission_not_found: "<red>Couldn't find any mission entry in the category, make sure all configs are assigned"
  set_success: "<green>Success</green>"

# Defines a category followed by its weight to be chosen when generating a random mission. Higher = more common
categories:
  easy: 50
  medium: 30
  hard: 20

mission:
  # What sounds to play on mission claim, leave empty to make it not play anything look at https://minecraftsounds.com/ to get sound keys and set up your pitch and volume
  claim_sound: "entity.player.levelup"
  # The pitch to use when playing the claim sound
  claim_sound_pitch: 0.80
  # The volume to use when playing the claim sound
  claim_sound_volume: 0.5
  # How targets are split when using the '[target]' placeholder, list types command and the type list on start, this will show like target1, target2 etc., include the space
  target_splitter: ", "
  # Number of blocks that have to be walked updating mission progress. Higher = better performance, lower = more accurate tracking
  update_walk: 5
  # How often (seconds) to clear the brew cache that tracks player brewing, don't make this shorter than the time it takes to brew a potion on your server
  brew_cache_timeout: 300

anti_abuse:
  # Track recently placed blocks to prevent farming break missions.
  recent_placement_cache: true
  # Maximum size of recent placement cache. Lower = better performance, higher = safer against farming.
  recent_placement_cache_size: 300
  # How often (seconds) to clear the recent placement cache.
  recent_placement_cache_timeout: 120
  # Track player step locations to prevent farming walk missions.
  recent_block_step_cache: true
  # Maximum size of the player step cache (per player). Keep small for performance.
  recent_block_step_cache_size: 5

menus:
  # What will be displayed to the player when using the /easymissions data command.
  data_menu: |-
```

Above is the config you'll see when you open the config.yml file, the options in there will be explained in further detail:

- **debug** Whether to toggle debug mode on or off, right now this does nothing except for include the stacktrace in config exceptions

**messages:**
- **reload** Message sent when the plugin is reloaded, no available placeholders
- **reload_fail** Message sent when the plugin encounters a configuration error while loading the config, no available placeholders
- **needs_player** Message sent when a command is run by console but requires a player such as the **/easymissions data** command, no available placeholders
- **needs_mission** Message sent when a command expects a mission item in your hand but doesn't find it, no available placeholders
- **give_mission** Message sent when a player is successfully given a mission whether using random, category-random or give, available placeholders are: **\<mission>** which will be replaced with the name of the mission config and **\<player>** which will be replaced with the name of the player that received it
- **rand_mission_not_found:** This message is sent when a category chosen either by random or category-random has no mission configs, no available placeholders
- **set_success** This message is sent when a successful change happens on a mission item with commands, like using **/easymissions set progress**, no available placeholders

**categories** This is just the list of categories you have that your mission configs can use, the numbers followed by them are weights and the higher they are the more common and vice versa for lower values. You can add as many categories as you wish

**mission**:
- **claim_sound** The sound to be played on mission claim, this takes a key for the sound, you can find other sound keys **[here](https://minecraftsounds.com/ "here")**, you may also leave this empty to not play a sound at all
- **claim_sound_pitch** The pitch of the sound to be played on mission claim, this takes a decimal value between 2.00 and 0.50
- **claim_sound_volume** The volume of the sound to be played on mission claim, this takes a decimal value between 1.0 and 0.0
- **target_splitter** How mission targets will be split when displayed in the list-types command, the [targets] placeholders, and the start-up log type list, using its default targets will be shown like this: ``target1, target2, target3...``
- **update_walk** How many blocks have to be walked before updating the progress of walk missions, as updating for every single block the player moves is too expensive and can quickly drain your server's performance, batching the updates helps mitigate this but introduces an issue where if the player logs out or takes too long before hitting the threshold their progress will be lost as it wasn't applied yet
- **brew_cache_timeout** Due to how the **brew** mission type is implemented there is a cache to track active player brews to increment their progress for brew missions, you can set how long the player stays in the cache to save more memory on your server or in case you have longer brewing durations than vanilla

**anti_abuse:**
- **recent_placement_cache** Whether to enable the recent place cache or not, disabling this will make missions with the **break** and **harvest** types vulnerable to players breaking and placing blocks to cheat missions
- **recent_placement_cache_size** How many blocks to store in the recent place cache maximum, don't make this too high to avoid huge memory usage
- **recent_placement_cache_timeout** How often to clear the recent place cache from its entries, it's basically a frequent clean-up, you shouldn't make this too long nor too short (makes it easier for players to just wait for it to expire and cheat again)
- **recent_block_step_cache** Whether to enable the recent step cache or not, disabling this will make missions with the **swim** **glide** **walk** types vulnerable to players just going back and forth in the same place without actually walking
- **recent_block_step_cache_size** How many blocks to store in the recent step cache **PER** player, don't confuse this with **update_walk** as this has no effect on it, don't make this too big to avoid huge memory usage and not too small so players cant just walk 2 more blocks and bypass it

**menus:**
- **data_menu** The message that will be sent to whoever runs the **/easymissions data**, available placeholders are **[here](#placeholders)**

---

#### missions/Any .yml file | Mission config
This contains all the required and options you will use to create missions from, missions will be linked to the config entries, the example.yml that comes with the plugin is a good example (who would've guessed) here:

```yaml
# EasyMissions - Mission config
# Any valid mission config file in the missions folder/directory will work. Feel free to categorize by file
# Each top-level key is stored on the mission item. Keep the keys short
# Because of the above, existing mission items with missing entries will not work
# For a full explanation and list of mission types, examples, and options, read the wiki and make sure to check default.yml
# This file can be deleted when you have another .yml file in the directory, it will regenerate when there is no valid mission configs in the directory

# Here is a mission that will utilize all possible options right now. Don't be afraid of how big it looks right now, it gets way shorter using defaults
break_stone: # Mission top-level key

  # Item display stuff
  name: "<gray>Stone Breaker</gray>" # Mission item display name
  completed_name: "<green>Stone Breaker (COMPLETED)</green>" # Name when completed, you can also use the placeholder [NAME] to add the normal name

  lore: # Mission item lore, there is a fair amount of placeholders available for you to use other than just this, check the wiki out to find a list of them
    - "<gray>Clear out the stone to make way for progress.</gray>"
    - ""
    - "<white>Progress: <green><progress></green>/<red><requirement></red></white>"

  completed_lore: # Lore when completed, [LORE!] in the first line will make it the normal lore but strikethrough
    - "<gray>You have cleared the path.</gray>"
    - ""
    - "<gold>Right-click to claim!</gold>"

  item_rarity: "COMMON" # The minecraft item rarity of the item (cosmetic only),
  item_material: "PAPER" # The type of item for the mission. It's recommended to keep this at paper and use item_model to change its texture to avoid issues, https://jd.papermc.io/paper/1.21.11/org/bukkit/Material.html for valid materials
  item_model: # Custom item model, leave empty or remove if you don't wish to have a custom texture, you can use existing textures like minecraft:stone too
  completed_item_model: # Item model when completed, leave empty or remove to default to item model

  # Mission settings
  category: "easy" # Must match a category in config.yml
  type: "break" # Mission type, see wiki for list
  requirement_min: 10 # Minimum possible requirement value
  requirement_max: 100 # Maximum possible requirement value
  blacklisted_worlds: ["world_the_nether"] # Worlds that this mission will not progress on.

  targets: # Valid targets to progress mission, what you can put here depends entirely on the selected type
    - "STONE"
    - "COBBLESTONE"

  # Commands to run on mission claim.
  # "say" will send a message to the player, not the minecraft command
  # "minecraft:say" runs the real minecraft command

  rewards:
    - "minecraft:say player completed a mission!"
    - "say hi!"
    - "give <player> cobblestone 64"

# Above is a full, functional mission with all options that should have a requirement between 10 and 100 and progresses when a player breaks stone/cobblestone

example_complex: # This is a complex mission minimal example, you can read more about it at the wiki but this is just a demonstration

  name: "<gold>Complex Mission</gold>"

  category: "hard"
  type: "complex_enchant" # this is the only complex type available in the plugin, addons may extend the available types and allow you to use more

  requirement_min: 1
  requirement_max: 1

  targets:
    - SHARPNESS 5 DIAMOND_SWORD


# Above is a minimal working complex mission, feel free to just not include the settings you don't need so you don't clutter things up.
# Additionally, you can configure the defaults of a mission to avoid repetition. Check default.yml for that
```
Above is the config you'll see when you open the example.yml file, the options in there will be explained in further detail:
- **Top-level key** The top level key is what specifies a mission config in the file and all options must be under this key, this key can be anything (Like in the example, it is break_stone and example_complex) but it's recommended to keep it short.

Now let's begin with the cosmetic part of the mission config:

- **name** The display name of the mission, can use any of the existing placeholders **[here](#placeholders)**
- **completed_name** The same as **name** with the same placeholders, just for when the mission is completed
- **lore** The lore that will be put on the mission item, can use any of the existing placeholders **[here](#placeholders)**
- **completed_lore** The same as **lore** with the same placeholders, just for when the mission is completed
- **item_rarity** The Minecraft **[Item Rarity](https://minecraft.fandom.com/wiki/Rarity "Item Rarity")**, this is basically useless, you can get the valid values for it **[here](https://jd.papermc.io/paper/1.21.11/org/bukkit/inventory/ItemRarity.html "here")**
- **item_material** The Material that the item will be, this is essentially what the item will be, like whether it'll be paper or a sword, look at **[This](https://jd.papermc.io/paper/1.21.11/org/bukkit/Material.html "This")** to get the names for your items to put here
- **item_model** The[ **Item model**](https://minecraft.wiki/w/Items_model_definition " **Item model**") that the mission item will have, if you leave this empty or delete it, it will use the normal texture provided by the selected item in **item_material**
- **completed_item_model** The same as **item_model**, just for when the mission is completed, if you leave this empty or delete it, it will use the **item_model**'s model

Now, for the logical part of the mission config which will determine its behaviour:
- **category** The category that this mission config will be under, must be a valid category from the ones you have in your **Main config\config.yml**
- **type** The mission type that your mission will have, this is very important and is what will determine what will progress your mission and what targets are considered valid for it
- **requirement_min** This is the minimum possible requirement that your mission can have, this cannot be larger than **requirement_max** or an exception will be thrown
- **requirement_max** This is the maximum possible requirement that your mission can have, this cannot be smaller than **requirement_min**, if you remove this it will default to the value of **requirement_min**
- **blacklisted_worlds** This is the list of names of the worlds you want your mission to not work on, if a player is within one of those worlds the mission will simply not progress
- **targets** This is the list of conditions or targets that are required for players to have for the mission to progress, what this can be and what it does or filters depends entirely on the **type** you choose, so if the type was **break** then you can put "STONE" in this list, and it will only progress when a player breaks a stone block. Using incorrect values for the type may cause an exception.
- **rewards** This is the list of commands to run when a player completes a mission and right clicks it to claim its rewards, the **say** minecraft command is replaced with the plugin's keyword which will send a message to the player directly, you may use **minecraft:say** if you wish to use the original say command. There is only one placeholder, and it is \<player> that will contain the player's name
---
#### missions/default.yml | Mission Defaults/Default Config
This contains the default options your mission configs will be using if the options aren't present in them, the options that were deemed not fit to be in defaults were removed, here is an example:
```yaml
# EasyMissions default mission config
# This is for missions with missing config entries and missing fields in mission configs.
# You may edit anything you wish here, any missing fields in a mission config will get its field from here.
# This file will be recreated each time it is deleted

default:
  name: "<gray>Default Mission</gray>"
  completed_name: "<green>[NAME]</green>"

  lore:
    - "<gray>Complete the requirements to get a reward.</gray>"
    - ""
    - "<white>Progress: <green><progress></green>/<red><requirement></red></white>"

  completed_lore:
    - "[LORE!]" # makes completed lore normal lore but strikethrough

  category: "easy"
  item_rarity: "COMMON"
  item_material: "PAPER"
  blacklisted_worlds: []
```

Above is the config you'll be seeing when you open the default.yml file the options in there will be explained in further detail:

- **default** If you paid attention in the mission config section, you'll notice that this is the top-level key, in the default config this can only be **default** and cannot be changed
- **name** The same as **name** in a normal mission config, it can use all the placeholders **[here](#placeholders)**
- **completed_name** The same as **name** but for when the mission is completed, shares the same placeholders
- **lore** The same as **lore** in the mission config, it can use all the placeholders **[here](#placeholders)**
- **completed_lore** The same as **lore** but for when the mission is completed, shares the same placeholders
- **category** The same as **category** in the mission config, must be a valid category name that is within your config.yml
- **item_rarity** The same as **item_rarity** in the mission config
- **item_material** The same as **item_material** in the mission config
- **blacklisted_worlds** The same as **blacklisted_worlds** in the mission config

You may feel free to adjust those and edit them to your liking and remove the options from your existing mission configs to make them use the existing defaults, this is how a mission looks like with defaults:
```yaml
test_potion:
  type: "potion"
  targets: 
    - "SWIFTNESS"
    - "STRENGTH"
  requirement_min: 1
  requirement_max: 2
```

Any removed option entry that exists in default.yml will use its default
# Placeholders
This is the list of placeholders used in the mission config, if you are linked to this section then the following will be all the placeholders you may use:
>NOTE: The placeholders are ones that are wrapped in <'s, do not confuse them with ones like **[NAME]** in **completed_name** as that is just a keyword for the plugin to help make your life easier

- **\<uuid>** The UUID of the mission
- **\<type>** The type of the mission
- **\<targets>** A list of valid targets within the mission or nothing, how the targets are separated is configurable using the config.yml\main config
- **\<progress>** The progress of the mission
- **\<requirement>** The required progress needed for the mission to be completed
- **\<percentage>** The completion percentage of the mission
- **\<config_id>** The mission's top-level key AKA config ID
- **\<completed>** Whether the mission is completed or not, will just display either "true" or "false"

---

