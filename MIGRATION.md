# MIGRATING

Actito 5.x is a complete rebranding of the Notificare SDK. Most of the migration involves updating the implementation from Notificare to Actito while keeping the original method invocations.

## Deprecations

- Crash reporting is now deprecated and disabled by default. In case you have explicitly opted-in, consider removing `re.notifica.crash_reports_enabled` or `com.actito.crash_reports_enabled` meta-data from your `AndroidManifest.xml`. We recommend using another solution to collect crash analytics.

## Breaking changes

### Removals

- Removed Scannables module.

### Dependencies

Starting with version 5.0, all SDK artifacts are now published to **Maven Central**.
You no longer need to include the Notificare Maven repositories, and all artifacts have been moved from the `re.notifica` namespace to the new `com.actito` namespace.

This migration simplifies dependency management and aligns the SDK with standard Maven publishing practices.
 
#### Step 1 — Update the root `build.gradle`

```diff
plugins {
-    id 're.notifica.gradle.notificare-services' version '1.0.1' apply false
+    id 'com.actito.gradle.actito-services' version '1.0.0' apply false
}

allprojects {
    repositories {
-        maven { url 'https://maven.notifica.re/releases' }
-        maven { url 'https://maven.notifica.re/prereleases' }
   }
}
```

#### Step 2 — Update the app `build.gradle`

Open your **app module’s** `build.gradle` file and align both the plugin implementation and dependency declarations.
Keep only the dependencies that you already use in your project.

```diff
plugins {
-    id 're.notifica.gradle.notificare-services'
+    id 'com.actito.gradle.actito-services'
}

dependencies {
-    def notificare_version = '4.x.x'
-    implementation "re.notifica:notificare:$notificare_version"
-    implementation "re.notifica:notificare-push:$notificare_version"
-    implementation "re.notifica:notificare-push-ui:$notificare_version"
-    implementation "re.notifica:notificare-inbox:$notificare_version"
-    implementation "re.notifica:notificare-user-inbox:$notificare_version"
-    implementation "re.notifica:notificare-in-app-messaging:$notificare_version"
-    implementation "re.notifica:notificare-geo:$notificare_version"
-    implementation "re.notifica:notificare-geo-beacons:$notificare_version"
-    implementation "re.notifica:notificare-assets:$notificare_version"
-    implementation "re.notifica:notificare-loyalty:$notificare_version"
-    implementation "re.notifica:notificare-scannables:$notificare_version"

+    def actito_version = '5.x.x'
+    implementation "com.actito:actito:$actito_version"
+    implementation "com.actito:actito-push:$actito_version"
+    implementation "com.actito:actito-push-ui:$actito_version"
+    implementation "com.actito:actito-inbox:$actito_version"
+    implementation "com.actito:actito-user-inbox:$actito_version"
+    implementation "com.actito:actito-in-app-messaging:$actito_version"
+    implementation "com.actito:actito-geo:$actito_version"
+    implementation "com.actito:actito-geo-beacons:$actito_version"
+    implementation "com.actito:actito-assets:$actito_version"
+    implementation "com.actito:actito-loyalty:$actito_version"
}
```

### Configuration file

If your project uses the **managed configuration** approach — meaning it includes a `notificare-services.json` file — you must rename this file to `actito-services.json`.

### Implementation

#### Adjust the constants in your `AndroidManifest.xml`
Open your `AndroidManifest.xml` file and rename all `re.notifica` constants to `com.actito`. For instance, here's an example of the changes to the trampoline intent:

```diff
<activity android:name=".MainActivity" android:launchMode="singleTask">
    <intent-filter>
-        <action android:name="re.notifica.intent.action.RemoteMessageOpened" />
+        <action android:name="com.actito.intent.action.RemoteMessageOpened" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

#### Rename references

You must update all references to the old Notificare classes and packages throughout your project.
Replace any class names starting with `Notificare` (for example, `NotificarePush`, `NotificareGeo`, `NotificareDevice`, etc.) with their Actito equivalents (`ActitoPush`, `ActitoGeo`, `ActitoDevice`, and so on).

Similarly, update all imports from the `re.notifica` namespace to `com.actito`.

For example, here’s how handling a trampoline intent should be updated:

```diff
- import re.notifica.Notificare
- import re.notifica.push.ktx.push
+ import com.actito.Actito
+ import com.actito.push.ktx.push

private fun handleIntent(intent: Intent) {
-    if (Notificare.push().handleTrampolineIntent(intent)) return
+    if (Actito.push().handleTrampolineIntent(intent)) return
}
```

> **Tip:**
> 
> A global search-and-replace can accelerate this migration, but review your code carefully — especially where custom extensions or wrappers reference old `Notificare` types or package names.

#### Refactor Java Compatibility Usages

If your project includes Java implementation files, you must remove outdated Notificare imports and replace them with their new Actito equivalents.
This ensures your project no longer references deprecated Java compatibility classes and aligns with the current SDK structure.

Classes such as `NotificareInboxCompat` have been removed.
Their Actito counterparts now expose static access points using `@JvmStatic`, allowing you to call functions directly from the module class.

For example:

```diff
- NotificareInboxCompat.getItems()
+ ActitoInbox.getItems()
```

#### Overriding Localizable Resources

If your project overrides SDK-provided localizable strings or other resources, you must update their names to align with the new Actito namespace.
All resource identifiers previously prefixed with `notificare_` should now use the `actito_` prefix instead.

For example, in your `res/values/strings.xml` file:

```diff
- <string name="notificare_default_channel_name">Notifications</string>
+ <string name="actito_default_channel_name">Notifications</string>
```

Ensure this change is applied consistently across all localized resource files (for example, values-es, values-fr, etc.) within your res directory.

> **Tip:**
> 
> A global search for `notificare_` in your `res/` folder will help you quickly locate and rename all relevant keys to the new `actito_` format.

#### Refreshing the inbox becomes a suspending function

The `refresh()` method from the `actito-inbox` module has been converted into a **suspending function**. This change ensures that the entire refresh process completes asynchronously, providing better control over execution flow and error handling within coroutines.

Example migration:

```diff
class MainActivity: AppCompatActivity() {
    private fun refresh() {
-        Notificare.inbox().refresh()
    
+        lifecycleScope.launch {
+            try {
+                Actito.inbox().refresh()
+            } catch (e: Exception) {
+                // Handle error
+            }
+        }
    }
}
```

#### Restricted Tag Naming

Tag naming rules have been tightened to ensure consistency.
Tags added using `Actito.device().addTag()` or `Actito.device().addTags()` must now adhere to the following constraints:

- The tag name must be between 3 and 64 characters in length.
- Tags must start and end with an alphanumeric character.
- Only letters, digits, underscores (`_`), and hyphens (`-`) are allowed within the name.

> **Example:**
> 
> ✅ `premium_user`  ✅ `en-GB`  ❌ @user


#### Restricted Event Naming and Payload Size
Event naming and payload validation rules have also been standardized.
Custom events logged with `Actito.events().logCustom()` must comply with the following:

- Event names must be between 3 and 64 characters.
- Event names must start and end with an alphanumeric character.
- Only letters, digits, underscores (`_`), and hyphens (`-`) are permitted.
- The event data payload is limited to 2 KB in size. Ensure you are not sending excessively large or deeply nested objects when calling: `Actito.events().logCustom(event, data)`.

> **Tip:**
> 
> To avoid exceeding the payload limit, keep your event data minimal — include only the essential key–value pairs required for personalized content or campaign targeting.

## Other changes

- Removed `NotificareEvent` data class. It was only intended for **internal** use and should not affect you.
- Removed `NotificareRegionSession` data class. It was only intended for **internal** use and should not affect you.
