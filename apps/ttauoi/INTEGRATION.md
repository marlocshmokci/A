# TTAuOI integration layer

TTAuOI is designed as a mod layer for a compatible, legally obtained TikTok Android APK.

## Injection points

- Profile / Privacy screen: insert a Mod entry at the top.
- Mod screen: open TTAuOI settings.
- Region provider: read ModSettings.region().
- Feed filter: apply likes/views/publication-age rules before rendering eligible feed items.
- Content switches: hide live streams, ads, feed photos and story photos.
- Theme adapter: apply the selected color to like/comment/follow controls.
- Restart gate: show the Android restart prompt after settings that require process recreation.

## Important

The standalone TTAuOI APK cannot modify another installed application's classes merely by being installed. A real TikTok mod build requires the target TikTok APK as input and a compatible patch/injection implementation for that exact build.
