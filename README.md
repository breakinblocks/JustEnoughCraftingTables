# Just Enough Crafting Tables

A crafting table that remembers what you put in it and lets you craft straight out of the chests next to it.

Place a Crafting Station beside your chests, barrels or drawers, open it, and a panel slides out beside the crafting grid showing what is in them. Grab what you need, craft, and put the rest back, all without leaving the crafting screen.

A Minecraft 1.21.1 mod for NeoForge and Fabric.

## Getting one

Put a vanilla crafting table in any crafting grid to get a **Crafting Station**.

Put a Crafting Station in a crafting grid to get two **Crafting Station Slabs**. Two slabs turn back into a station.

The station looks and behaves like a crafting table, with brass corner fittings so you can tell it apart from a vanilla one. The slab is a half-height version that does everything the full station does.

## What it does

### It keeps your grid

Items you leave in the crafting grid stay there when you close the screen. They show up lying on top of the table, so you can see at a glance what a station is set up for. Break the station and the items drop on the ground.

Everyone shares the same grid, so a station in a team base works like a shared workbench.

### It reaches into the inventories around it

Any block that stores items and touches the station (above, below or on any side) gets its own tab at the top of the panel. Chests, barrels, drawers, backpacks on the ground, machines: if it holds items, it can show up.

Each tab always sits in the same spot for the same side of the station, so the position tells you where that inventory is. The tab shows the block it belongs to, and hovering over it tells you its name and which side it is on.

Click a tab to see what is inside. From there you can:

- take items out by clicking, just like a chest
- put items in by clicking them into an empty or matching slot
- shift-click an item to move it into your inventory

Shift-clicking something from your own inventory puts it into the crafting grid.

Place or break a chest next to the station while the screen is open and its tab appears or disappears straight away.

### It handles huge storage

Storage controllers and drawer walls can hold thousands of slots. The panel shows them a page at a time, with a scrollbar for each page and page buttons at the bottom.

- Scroll the mouse wheel over the panel to move up and down.
- Hold Shift while scrolling, or press Page Up and Page Down, to flip pages. Home and End jump to the first and last page.

A search box at the bottom of the panel finds items across every connected inventory at once. Type part of a name, `@` followed by a mod name to find everything from that mod, or `#` followed by a tag. While you search, each tab shows how many matching items it holds, and tabs with nothing matching fade out. Press Ctrl+F to jump to the search box and Esc to clear it.

When you reopen a station, it remembers which page you were on and what you searched for, until you leave the game.

### Store and clear

Two buttons sit to the right of the crafting result:

- **Store** sends everything in the grid back where it came from. Items you took from a chest go back to that chest, and items from your inventory go back to your inventory. Anything that does not fit stays in the grid.
- **Clear** moves everything in the grid into your inventory.

If the chest an item came from has been removed, Store leaves that item in the grid. Use Clear to take it.

### Recipe viewers

With JEI or EMI installed, the recipe transfer button fills the station's grid using items from your inventory and from every connected inventory, including tabs you are not looking at and pages you have not scrolled to. Recipes you can make show up as craftable even when the ingredients are in a chest next to the station.

This works even when the server does not have JEI installed.

### Where ingredients come from

A toggle button below the Store and Clear buttons chooses where filled recipes take their ingredients from:

- **Your inventory first** (the default): your main inventory, then your hotbar, then the connected inventories.
- **Connected inventories first**: the chests and storage around the station, then your inventory.

Items already in the grid are always used first. The station remembers your choice.

### Repeat crafting

Turn on the repeat button and shift-clicking the result keeps crafting as long as it can. Each time the grid runs out of an ingredient, it refills that slot from your inventory and the connected inventories, in the order chosen above. It stops when you run out of ingredients or inventory space.

Leftovers like empty buckets are put away automatically so crafting can carry on.

### Hiding an inventory

Right-click a tab to make the station ignore that inventory. This is handy for a furnace or machine that happens to sit next to your station. The tab fades out and the station stops showing, searching and pulling from it. Right-click the tab again to bring it back.

Hidden sides are saved on the station, so they stay hidden for everyone.

## Dyeing

Sneak and right-click a station or slab with any dye to paint it. The wood takes the colour while the tools and brass fittings keep theirs. Dyes from other mods work too.

- A dyed station keeps its colour when you break it and place it again.
- Crafting slabs from a dyed station gives slabs of the same colour, and two matching slabs make a station of that colour.
- To remove the colour, wash the item in a water cauldron.
- A slab only joins onto another slab of the same colour.

## Slabs

The Crafting Station Slab can go on the top or bottom half of a block, and can be placed in water. Put a second matching slab on top to make a full double slab. A double slab is still one station with one shared grid.

## Settings

Open the mod's settings from the Mods screen. You can:

- move the panel to the right side of the crafting screen, which helps if your JEI bookmarks sit on the left
- turn off the items drawn on top of the table
- choose whether the mouse wheel scrolls anywhere over the panel or only over the scrollbar

Server owners have a few more options, such as how many slots fit on a page and whether players can put items into connected inventories from the station.

## Works well with

- **JEI** and **EMI**: recipe transfer straight from your storage
- **Crafting Tweaks**: its rotate, balance and clear buttons work on the station grid
- **Polymorph**: choose between recipes that share the same ingredients
- **Jade**: look at a station to see its colour and what is on its grid
- **Mouse Tweaks**: its scroll and drag tricks work on the panel's slots

The screen's tabs, buttons and search box also work with Minecraft's narrator.

## Coming from Crafting Station

Just Enough Crafting Tables recognizes Crafting Station blocks, slabs, saved grids and inventory items.

A few things will look different afterwards:

- Stations become full crafting-table blocks instead of tables on legs.
- Items on slabs now face the right way.
- Store sends items that were already in a grid to whoever presses it.

The switch is one way. **Back up your world first.** Once the world has been saved with this mod, going back to Crafting Station will lose your stations.

The two mods cannot be installed together. The game will refuse to start if both are present.

## Good to know

- Stations do not connect through other stations, so placing two side by side does not chain them together.
- Hoppers and pipes cannot put items into or take items out of the crafting grid.
- A station only sees blocks it is touching, not diagonals or blocks further away.

## License

Just Enough Crafting Tables is available under the PolyForm Shield License 1.0.0 AND the PolyForm Perimeter License 1.0.0. Use must comply with both. See [LICENSE.md](LICENSE.md).
