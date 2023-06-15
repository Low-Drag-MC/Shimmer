# Shimmer

![logo_full_ alpha_bg](https://github.com/Low-Drag-MC/Shimmer/assets/26162862/ecc3a347-2e41-4ba2-a426-d5b36eab503c)

<blockquote style="text-align: center;">
Shimmer is a mod concentrated on rendering effects<br>
fantasy bloom and colored light<br>
both configurable json and java api support<br>
customize what you like
</blockquote>

Join our [![discord](https://shields.io/badge/Discord-Join%20Us-blue?logo=Discord&style=flat)](https://discord.gg/CQywkfwPrC) for communication, issue feedback  

<h3 align="center">
<a href="https://modrinth.com/mod/shimmer!" target="_blank">Modrinth Homepage</a> |
<a href="https://www.curseforge.com/minecraft/mc-mods/shimmer/" target="_blank">CurseForge Homepage</a>
</h3>

## Bloom
![image](https://user-images.githubusercontent.com/26162862/201468458-021dedc4-d883-44d2-961a-d2f38110fae4.png)  
![image](https://user-images.githubusercontent.com/26162862/201468409-8c2a50f3-f2a9-435a-b6d5-bed59a69a1c3.png)  
![image](https://user-images.githubusercontent.com/26162862/201468589-eea3553d-1424-4b58-9ac5-083660187677.png)  

for compability with [MadParticle](https://github.com/USS-Shenzhou/MadParticle)  
![Jnm1LVVLyq17t0us](https://user-images.githubusercontent.com/26162862/201468947-60a1d80f-b9f4-4d4f-a130-ac94fbb3efad.gif)  

## ColoredLight
![image](https://user-images.githubusercontent.com/18493855/170466919-65ca5b39-7397-45f9-99ed-c9a8d72cec70.png)  
![image](https://user-images.githubusercontent.com/26162862/201469162-5daeff42-8450-4cc7-a3f8-6e4ed0e7ceb2.png)
for compability with  [Lighting Wand](https://www.curseforge.com/minecraft/mc-mods/lighting-wand)  
![image](https://user-images.githubusercontent.com/26162862/201469108-4d48a6ea-422b-4218-8463-3e89484f7901.png)  
video [here](https://www.youtube.com/watch?v=-OXVb8edD7o&ab_channel=Snownee) 


## Configuration

shimmer also have a powerful configure system, both for no-code and java API, see **wiki** for detailed information  
What's more, we also have powerful gui for quick configuration  

### Auxiliary Screen
![gif](https://user-images.githubusercontent.com/26162862/195899429-75fb0db4-a919-4a4b-bb0d-b7bb4c8b7222.gif)

### Eyedropper
![gif](https://user-images.githubusercontent.com/26162862/199932496-056e548b-bfc6-4a46-bef3-d9843782d165.gif)


## For Modder

In order to import shimmer into your development enviroment

```gradle
repositories{ //add our maven first
    maven {
        url "https://maven.firstdarkdev.xyz/snapshots"
    }
 }

// Forge
implementation fg.deobf("com.lowdragmc.shimmer:Shimmer-forge:${minecraft_version}:${shimmer_version}")
// Fabric
modImplementation "com.lowdragmc.shimmer:Shimmer-fabric:${minecraft_version}:${shimmer_version}"

//for architectury user, you can also just import shimmer's common jar in you common module
modImplementation "com.lowdragmc.shimmer:Shimmer-common:${minecraft_version}:${shimmer_version}"
```
## After All
great thanks [resourcepack](https://www.curseforge.com/minecraft/texture-packs/blooming-blocks) made by WenXin2'S 
