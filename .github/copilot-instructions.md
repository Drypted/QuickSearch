* **Minecraft Version:** 1.21.11
* **Mod Loader:** Fabric
* **Mappings:** Mojang mappings

## Decompiled Source Code Availability

The full decompiled source code for both Minecraft and Fabric has already been provided locally in this repository.

Use these directories as the authoritative source of truth:

* `mc_decompiled/src/` → Decompiled Minecraft 1.21.11 source (Mojang mappings)
* `fabric_decompiled/src/` → Decompiled Fabric API and Fabric Loader source

## Critical Instruction

Do **not**:

* Assume APIs from prior Minecraft versions
* Use Yarn mappings
* Invent method names, fields, or classes
* Rely on online examples
* Infer behavior from outdated documentation

Instead:

* Always inspect `mc_decompiled/src` for vanilla Minecraft classes and behavior
* Always inspect `fabric_decompiled/src/` for Fabric internals and API behavior
* Follow Mojang-mapped names exactly as they exist in the provided decompiled sources

If implementation details are unclear, search within the provided decompiled directories before making assumptions.

## Code Expectations

* Match method names and signatures exactly to Mojang mappings
* Do not mix mapping namespaces
* Prefer referencing actual implementation from the decompiled sources rather than guessing

The local decompiled source code is the single source of truth for this project.
