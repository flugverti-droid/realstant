# Real Stant Native v9 — graphics sources

The renderer itself is written in Java + OpenGL ES 3.0 and does not use HTML, JavaScript or WebView.

For future imported 3D assets, the project can use permissively licensed sources such as:
- Kenney Car Kit — CC0: https://kenney.nl/assets/car-kit
- Poly Haven — CC0 PBR textures / HDRIs / models: https://polyhaven.com/
- Khronos glTF Sample Assets — selected examples include CC0 assets: https://github.com/KhronosGroup/glTF-Sample-Assets

Current v9 intentionally keeps the runtime self-contained and procedural so the APK does not depend on an online CDN.

## Included model resource
`models/RealStantCar.glb` is a native GLB 2.0 vehicle resource generated for this project.

## Online reference model
Khronos Toy Car (CC0 1.0) is documented as an optional high-quality reference asset. It uses clearcoat, transmission and sheen material extensions.
