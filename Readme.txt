

How to setup AndEngine in Android Studio 

This guide assumes that you have Android Studio, git, a terminal and rsync installed.
1. Create and open the project in which AndEngine will be used.
2. Create a module for AndEngine.
File ? Project Structure ? Modules ? + ? Android Library ? [Module name: AndEngine, Package name: org.andengine, Create activity: no] ? Finish
3. Tell the compiler to search the AndEngine module by adding a dependency. 
File ? Project Structure ? Modules ? <the module that will use AndEngine> ? Dependencies ? + ? Module dependency ? :AndEngine
4. Download AndEngine by running the following in a terminal: 
$ cd <project folder>
$ git clone -b <GLES2 or GLES2-AnchorCenter> https://github.com/nicolasgramlich/AndEngine.git AndEngine.github
$ rsync -a AndEngine.github/* AndEngine # This merges AndEngine.github and AndEngine
$ rm -rf AndEngine.github
5. Make a minor change to the file tree of AndEngine to make it work with Android Studio.
$ cd <project folder>/AndEngine/src/main
$ rm -r java/org
$ mv org java
6. Ensure that Android Studio notices the changes you did. Do this by right clicking on the AndEngine module in the Project tool window and choosing Synchronize 'AndEngine'.
You should now be able to use the core AndEngine package.
Optional: adding an extension
Adding an extension is much the same procedure, so I'll only list the additional things you need to do.
* Before 2: To find the package name of the extension, browse the extension repo on github and look in AndroidManifest.xml.
* After 3: Make the extension dependent on AndEngine.
* At 5: This step may vary slightly. You may not need to do anything.
Fixing java.lang.UnsatisfiedLinkError
When I tried to use the physics extension, everything compiled fine, but when I ran my app, it crashed, logging: java.lang.UnsatisfiedLinkError: Couldn't load andenginephysicsbox2dextension from loader dalvik.system.PathClassLoader[...]: findLibrary returned null

That is, the .so files are not in the apk. Workaround:
1. Manually copy <extension-module>/libs/* to a new folder <main-module>/lib
2. Compress lib/ to lib.zip
3. Rename lib.zip to lib.jar
4. Add compile files('lib.jar') as a dependency in <main-module>/build.gradle
However, it should be noted that developer needs to update lib.jar manually in the future!



