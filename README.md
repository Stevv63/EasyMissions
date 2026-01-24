<div align="center">
<h1>EasyMissions – Missions / Scrolls Plugin</h1> 

> **NOTE:** This plugin is in **beta**, there may be bugs present.

#### A PaperMC missions plugin aiming to use the latest Paper APIs while staying configurable, supporting FoliaMC (Unstable), performant (I wish), and easy to work with.
</div>

## Table of Contents
- [Important Notes](#some-things-you-need-to-know-please-read)
- [Features](#why-choose-easymissions--features)
  - [Mission Types](#powerful-mission-types)
  - [Custom Options](#custom-mission-options)
  - [Anti Exploit](#anti-exploit-features)
  - [Wildcard Targets](#wildcard-matching-for-targets)
  - [Mission Defaults](#mission-defaults)
  - [API](#api)
- [Commands](#commands)
- [Placeholders](#placeholders)


## Some things you need to know, Please read.
To begin:

Due to the current stability state of EasyMissions which is.. now **beta!**, the previous alpha warning is mostly invalid, stability is not a guarantee due to it not being release yet obviously but it means we are close, and it shouldn't be too bad I think

Breaking changes will be noted in each release.

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
- Mission configs will be **FURIOUS** if you misconfigure them, you will get a large error header that explains where, why, and if a rollback happened.
- If a mission config is missing certain options or is invalid, **The mission will not function**.
- If you reload your config and break mission configs, the plugin will just use the working version from before.

Due to the goals of EasyMissions, there is **NO** older version support, no spigot/bukkit support and no guarantee that newer api usage as updates happen won't break it on the versions it already supported. As of now EasyMissions works only on:

- PaperMC and probably most of its derivatives/forks
- FoliaMC (Considered unstable, race conditions might exist, and I'm not familiar with folia that much, but I can tell you it starts and I didn't run into any issues so far)
- At least 1.21.5 Minecraft version (This is according to the PaperMC diff, the minimum version I tested it on was FoliaMC 1.21.8)
-----

#### Now that we've gone through the base and if you are still interested, let's take a look at what this thing really offers.

## Why choose EasyMissions | Features:
Generally it is a matter of what you're comfortable with, however EasyMissions may offer some unique features such as:

### Powerful Mission Types
---
Types are the most important in the plugin and the second most important part of a mission item's identity after the config entry it belongs to. they specify what requirements you may have which dictates what the player needs to do to progress the mission.

There is **23** available mission types ranging from things as simple as "kill 3 mobs" to "apply sharpness 5 to a netherite sword that previously had any enchant of level 3 and has a custom pdc tag".


The MissionType implementation allows you to do a lot of things with EasyMissions, types are given full freedom to a `targets` (or if not present, the config's section) section where they can parse and read the data they expect.

This allows developers making their own types to have as much power as needed, just create the class, register its instance and pass it into the `findAndProgressMission` method to make missions progress for your type.

For Creating types, error handling is handled by the plugin itself and any errors you throw will have your error message printed out in an informative way for the admin.

Type configuration is also not that hard, once you are familiar with `matchers` you will be able to work with most types in config if not all.

A list of documentation on types is available in the wiki.

When the developer (me typing this) is satisfied with the state of the plugin, there will be addons that will extend types and their compatibility with other plugins so that you aren't limited to the vanilla internal ones

### Custom Mission Options
---
Custom options are options that can be put on any mission config, they are mainly registered by other plugins.

An example of what a custom option can do is: say you're making an addon to the plugin that adds WorldGuard compatibility, you want to create an option for admins to use to limit progression of a mission to a certain region, you use a custom option for that.

All custom options are under the `custom_options` section

There is only one default custom option that comes with the plugin: `permission`

An example of using it is

```yaml
  custom_options:
    permission:
      values:
        - "easymissions.break"
```
The `values` list of permissions allows you to limit the permissions to only the permissions in that list.

### Anti Exploit Features
---
There are mission types that are really, really easy to exploit one of them is walk missions, you can just walk back and forth, and you will finish the mission in no time, or break missions where you can just place the block, break it, repeat instead of actually looking for that block.

This issue, will break your economy if you use them unwisely.

Right now there are 2 available mitigations for this:

- **The recent place cache**  a very configurable cache that lets you set its size and its clean up frequency, when a player breaks/harvests a crop or block they placed intentionally in an attempt to cheat the system, the cache will realize that the block was recently placed by a player and will not count it as progress

- **The recent step cache** another configurable cache that lets you set its size per player, when a player walks back and forth or in a circle (depends on how big you configure the cache to be) the cache will realize they're not going anywhere and not count as progress

Some types will **NOT** have anti exploit features such as:

- **place** The tracking and association of a mission to a block break and then doing something like decrementing it seems complicated to me and I don't know how it would be reliably and efficiently implemented. The issue is that we are trying to revert progress instead of just checking before adding it like the existing solutions, I'm open to solutions and suggestions on how it could be done if you have a new idea.

- **craft** It's possible to cheat the system if you had a mission that required crafting 7 iron blocks, the player could do it with only 9 iron ingots and just craft them into block and ingot back and forth. it's complicated for me to figure out a way to actually make this without doing dirty things like tagging items or expensive tracking/heuristic, it's best for you to use one way recipes in your targets like pickaxes. I'm open to solutions and suggestions on how this could be done if you have a new idea too

Keep in mind that the following types may have exploits and could use more testing:
- **disenchant** Uses inventory events to get whether you disenchanted the item or not
- **enchant (While using Anvil)** Uses Inventory events to get whether you enchanted the item or not
- **repair** Uses Inventory events to get whether you repaired the item or not

Right now I'm not aware of any other ways to cheat the system available other than the ones already fixed, if you find any please make an issue and if you have a new idea, make an issue with the suggestion tag too!

### WildCard matching for targets
---
Imagine you want to make a mission that wants the player to mine 90 blocks of ANY ore, you would go and type out every. single. ore. that exists in the game, this looks ugly in your config, and it just isn't fun to do, for that reason there is wildcard matching in the EasyMissions types, now instead of typing every ore out you just do:

```yaml
type: break # the break type only has one available condition that you may or may not set, and it is "materials"
materials: # we only specify one requirement, no need for a full "targets" section. 
  - *_ORE
```
this will increment for any block ending with ore, so diamond ore, iron ore etc. and their deepslate variants, and it's all in a single line!

The limitations are:
- Wildcard matching is basic for now, you can do things like \*\_ore or diamond\_\* or just \* or \*diamond\* for contains, more complex or specific wildcards that contain more than two *'s however will not work and ? doesn't work either. Though I believe the existing functionality should cover most if not all your usage needs for minecraft matching

### Mission Defaults
---
The creation of a mission config can be a lot of work and typing, too many fields that you just wish the plugin didn't have, sometimes you just need to use one default for things.

The defaults allow you to set things such as the mission material, itemrarity, lore and name and their completed variants, and the category of the mission and more stuff!

That is where the **default.yml** comes in, options that aren't in the mission config will use the value from the default.yml so you don't have to explicitly specify everything even if it's always the same across all missions.

So for example, you don't even have to make a custom lore for each config! just specify `task` in your mission configs and use the placeholder `task` in your default.yml's lore and now each mission has a unified view while still explaining what you need to do!

Now there are some options and things that just cannot be defaulted to, such as the type or the targets, the item model, its completed variant and the rewards.

### API
---
The API section has its own wiki page that you may check out to learn more about how to interact with the plugin and do your own thing.

A quick overview of what the API allows you to do is:

- **Events** As of now there is a `MissionClaimEvent` and `MissionProgressEvent` that you may listen to and cancel
- **Custom Types** You can register your types by following the examples and using the type registry, just like native ones
- **Custom Options** You can register your own options by following the examples and using the options registry

And a lot of other methods that may help you

### Ability to organize missions
----
Any mission config in a valid .yml file in the missions directory will be loaded by the plugin, this allows you to categorize missions and not deal with the burden of having one huge missions file that contains everything, instead you can split the files by mission type, targets, category or however you really want as long as it is a valid mission config in a .yml file in the missions directory

### Custom textures
---
You can set custom textures for your mission items very easily by using the **item_model** and **completed_item_model** options in the mission config, you will use your resourcepack's namespace followed by the texture you wish to use, you can also use existing Minecraft textures like minecraft:stone to use the minecraft stone texture, it is recommended that you use **item_model** instead of changing the **item_material** option, changes to those will also be applied on all existing missions with that config entry for everyone on the next time they progress

An example of how to use them is:

```yaml
item_model: minecraft:stone # This will set the item's texture to stone regardless of its base item, it will have the behavior of its base item but with a different texture

completed_item_model: minecraft:diamond_block # This will set the item's texture to a diamond block when it is completed
```

**item_model** defaults to the base item's texture when it is empty/unset

**completed_item_model** defaults to **item_model** if empty/unset which will also default to the base item's texture if empty/unset


### Configurability
---
EasyMissions tries to make it so that everything is easier for the admin working on the plugin or API while keeping stuff powerful, you can configure almost every part of the item's display visible to the players such as:

- **Name and its completed variant** The display name for the mission depending on whether it's completed or not
- **Lore and its completed variant** The lore for the mission depending on whether it’s completed or not
- **Texture and its completed variant** The mission item's texture
- **Requirement min-max range** You can set a range for mission requirements to generate betweem, like `1-5` or just `1` or even `5+` for any number greater than 5 or `-5` for any number less than 5
- **Blacklist worlds** Allows you to blacklist worlds to prevent missions from progressing in them, you can blacklist as many as you want
- **Custom Options** Those are options added by addon plugins using the API, there is a default one in the plugin called `permission` for you to limit missions to certain permissions only

And many, many other options, those are the ones I believe are worth mentioning most. Feel free to check out the example.yml for a comprehensive list

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

# Placeholders
This is the list of placeholders used in the mission config, they allow you to make detailed descriptions for your missions in their lores or names.

[Placeholders are now here](https://github.com/Stevv63/EasyMissions/wiki/Placeholders)
---

