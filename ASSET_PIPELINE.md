# RealStant 9.0 — bundled asset pipeline

This build moves visual game resources into the APK `app/src/main/assets/` bundle so the renderer does not have to construct every vehicle/prop mesh at runtime.

## Bundled resources
- 7 static vehicle OBJ meshes: BMX, PITBIKE, SCOOTER, MTB, ATV, MINI BIKE, BOARD
- park OBJ meshes: ramp, trampoline, rail, box, landing, platform
- environment OBJ meshes: tree, rock
- lightweight 128x128 material textures: asphalt, ground, concrete, metal, rubber, wood, green, red, blue, orange, UI
- existing route/config data from the project is preserved
- `manifest.txt` contains the exact preload list

## Loading behavior
After registration/login, the first run shows a native loading screen with file count and percentage. Every bundled file is opened once to warm the asset package. The completion flag is stored in SharedPreferences. Future logins skip the screen unless the asset-bundle version key changes.

## Licensing / source review
For open-asset research, the project was checked against CC0-oriented libraries such as Poly Haven and Kenney/OpenGameArt. Their licensing pages describe CC0/public-domain-style reuse for the relevant asset collections. The new vehicle/park meshes included in this build were generated specifically for RealStant as lightweight static OBJ assets rather than copied from a protected game.

## Performance target
Assets are intentionally low-poly and small. The renderer keeps a fallback procedural model path if an OBJ cannot be loaded, so a missing asset should not make the game fail to start.
