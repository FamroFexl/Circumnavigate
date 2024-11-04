<font size = "4">**Say farewell to immersion-breaking world borders, and greetings to wrapped worlds! This mod visually and functionally tiles a finite world so that you can walk across from one border to the other.**</font>
<br>

<ins>**Please note this is an ALPHA. No stable or expected functionality or future support is guaranteed.**</ins>

## Building
`gradlew build`

##  Tutorial:<br>
Watch [this video](https://www.youtube.com/watch?v=bmkUSeLEE7Y) or read the [wiki](https://github.com/FamroFexl/Circumnavigate/wiki) for setup information.

## Updates:
**Current Functionality:**
1. Wrapped chunk sending and loading
2. World wrapping settings GUI
3. Wrapped block destruction/placement
4. Wrapped player positioning (for proper block collision and interaction distance)
5. Entity Handling (teleportation, hitboxes, spawning, riding)
6. Vanilla client support
7. Redstone/block update handling (redstone, explosions)

**Future Updates (🔴 Alpha):** (not exclusive)
1. Entity handling (pathing)
2. Proper nether limits and portal handling
3. Proper spawning and respawning for irregular world limits. (Limits past (0,0))
4. Redstone/block update handling (light updates, etc.)
5. Stopping over-bounds chunk generation and storage
7. Wrapped chunk shifting? (for fun)

**Future Updates (🟠 Beta/ 🟢 Release):**
1. Wrapped coordinate support in commands?
2. Map item support?
3. Overworld wrapped world generation
4. Nether wrapped world generation
5. Wrapped structure generation
6. Curvature shader?
7. Developer API?
8. Datapack coordinate wrapping?


## Tools
**Debugging:**
1. Coordinates and wrapping information can be seen in the debug menu. Actual coordinates will only show up if the client is past a bounds. <br><br>
   _Wrapping Settings:_<br>
   ![Wrapping Settings](https://cdn.modrinth.com/data/cached_images/63223899ff1dc90d88d9f2d3d2a92dc5fff77a52.png)<br>
   _Actual Coordinates:_<br>
   ![Actual Coordinates](https://cdn.modrinth.com/data/cached_images/8459b9c4cbc31029cf8bacc6859c6d18fbdfabab.png)<br><br>
2. Chunk boundaries now show purple and pink lines to visualize chunk borders.<br><br>
   _Purple Lines Showing Distant Chunk Border:_<br>
   ![Purple Lines](https://cdn.modrinth.com/data/cached_images/d6c82034730d44ada5b072dded8c4a639dd66d7f.png)<br>
   _Pink Lines Showing Immediate Chunk Border:_<br>
   ![Pink Lines](https://cdn.modrinth.com/data/cached_images/657bbd9156a338134939835d84534ce13040be1a.png)

