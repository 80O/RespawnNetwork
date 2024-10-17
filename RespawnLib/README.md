# RespawnLib README - Seriously, read this!
Thank you for using ***RespawnLib*** the library of common methods, functions and mechanics made by and for the
_RespawnNetwork_! Please mind that this is a *bukkit* library, so it's not intended for other projects that won't be
bukkit plugins.

# Table Of Contents
    1. Prepare to use it
    1.1 Installing maven
    1.2 Prepping your editor
    1.3 JDK7 and higher
    2. How to use it
    2.1 Common stuff
    2.2 Message API
    2.3 Game API

# Preparing your environment
Before using the RespawnLib you might need to prepare a thing or two.

## JDK7 and higher
Be sure to have at least JDK7, a higher version is highly recommended.
You can download the latest JDK8 right here:
[Orcacle JDK8 Downloads](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

When you're done with the installation, we're now ready to install something called "Maven"

## Installing Maven
If you already now what Maven is, you can skip this paragraph, if not, be sure to read this - Maven is awesome! Almost
every programmer needs some common code after a while, let it be something you need in all of your projects. Maven helps
out with that. It's a build system that you can use to manage your libraries and automate the build process! Want the
Bukkit-API in your project? Add that. Want to have easier Database support? Add that. Oh Apache commons? Add that.
It's easy. You can read more about maven on their [website](maven.apache.org).

So let's start installing maven.

### Windows
Visit [this Maven official website](http://maven.apache.org/download.html), choose a version and click on the download
link, e.g `apache-maven-2.2.1-bin.zip`.

Extract the downloaded zip file. In this case, we extracted to the *c* drive and renamed the folder, e.g ``C:\maven``.

Add a new ``MAVEN_HOME`` variable to the Windows environment, and point it to your Maven folder.

![Image](http://www.mkyong.com/wp-content/uploads/2009/11/maven-maven-home.png)

Also update the PATH variable, append “Maven bin folder” path, so that you can run the Maven’s command everywhere.

That’s all, just folders and files, a real installation is *NOT* required on Windows.


### Mac
OS X prior to Mavericks (10.9) actually comes with Maven 3 built in. If you're using mavericks, just follow the very
simple steps:

#### Using MacPorts
If you're using MacPorts you can just do the following:

    sudo port install maven3
    sudo port select --set maven maven3

#### Using Homebrew
Almost the same as MacPorts:

    sudo brew install maven

#### Manual install
Eh, you might wanna look at [this](http://stackoverflow.com/questions/19678594/maven-not-found-in-mac-osx-mavericks).


### Linux
You can install maven through your package manager. If it's not in there, please use the same tutorial as the Mac users,
since Mac and Linux are based on the same system architecture (UNIX).

### Checking the installation
Assuming qualifications are met, run "mvn -version" in a terminal or command prompt and see some output like this:

    Apache Maven 3.0.3 (r1075438; 2011-02-28 12:31:09-0500)
    Maven home: /usr/share/maven
    Java version: 1.6.0_29, vendor: Apple Inc.
    Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
    Default locale: en_US, platform encoding: MacRoman
    OS name: "mac os x", version: "10.7.2", arch: "x86_64", family: "mac"

### Adding the 'settings.xml'
**Please ask a developer for that, we don't want to put our passwords in here!**

## Preparing your editor
### Eclipse
You just need to install the m2e plugin from the eclipse marketplace, that's it.

### IntelliJ or Netbeans
Nothing. They support it out-of-the-box ;)

# How to use the library
Since this is a library, this project will not run without a real plugin for a bukkit server.
Be sure to check the JavaDocs before asking somebody about how to use thing XYZ. You've been warned!

## You own project file
As in every maven project you need a so called "pom.xml" It describes what you need as libraries, the name of your
project and some other build-related things.

For now, just use this example file:

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <!-- Project information -->
        <groupId>com.respawnnetwork</groupId>
        <artifactId><your game name, ALL LOWER CASE!></artifactId>
        <name><A human readable name></name>
        <version>1.0-SNAPSHOT</version>
        <packaging>jar</packaging>

        <!-- Properties -->
        <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

            <jdk.version>1.7</jdk.version>
        </properties>

        <!-- Repositories -->
        <repositories>
            <!-- Respawn repository -->
            <repository>
                <id>rn-releases</id>
                <url>http://build.respawnnetwork.com:8301/content/repositories/releases/</url>
                <releases><enabled>true</enabled></releases>
                <snapshots><enabled>false</enabled></snapshots>
            </repository>

            <repository>
                <id>rn-snapshots</id>
                <url>http://build.respawnnetwork.com:8301/content/repositories/snapshots/</url>
                <releases><enabled>false</enabled></releases>
                <snapshots><enabled>true</enabled></snapshots>
            </repository>
        </repositories>

        <!-- Dependencies -->
        <dependencies>
            <!-- Spigot API (includes bukkit) -->
            <dependency>
                <groupId>com.respawnnetwork</groupId>
                <artifactId>respawnlib</artifactId>
                <version>1.0</version>
            </dependency>
        </dependencies>

        <!-- Distribution management -->
        <distributionManagement>
            <repository>
                <id>rn-releases</id>
                <name>Releases</name>
                <url>http://build.respawnnetwork.com:8301/content/repositories/releases</url>
            </repository>

            <snapshotRepository>
                <id>rn-snapshots</id>
                <name>Snapshots</name>
                <url>http://build.respawnnetwork.com:8301/content/repositories/snapshots</url>
            </snapshotRepository>
        </distributionManagement>

        <!-- Build settings -->
        <build>
            <plugins>
                <!-- Define JDK for compilation -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.1</version>
                    <configuration>
                        <source>${jdk.version}</source>
                        <target>${jdk.version}</target>
                    </configuration>
                </plugin>

                <!-- Shading to include the compilation output of some dependencies -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-shade-plugin</artifactId>
                    <version>2.2</version>
                    <configuration>
                        <outputDirectory>${project.build.directory}/original</outputDirectory>
                        <createDependencyReducedPom>false</createDependencyReducedPom>
                    </configuration>
                    <executions>
                        <execution>
                            <phase>package</phase>
                            <goals>
                                <goal>shade</goal>
                            </goals>
                            <configuration>
                                <artifactSet>
                                    <excludes>
                                        <!-- Exclude spigot - we don't need that -->
                                        <exclude>org.spigotmc:*</exclude>
                                    </excludes>
                                </artifactSet>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </project>


## Common stuff
We have some pretty cool helper functions to help you out with scoreboards, the token system, mojang accounts, items and
much more. Just look at them, they should be pretty self-explanatory.

As a general rule for all your plugins: You have to (well, at least should) extend the `Plugin` class instead of the
normal JavaPlugin one. It provides a few more stuff and helpers for your project.

## Message API
To send simple messages you can do the following:

    Messages.INFO.send(player, "Hey, you there!");

To customize it a bit more you can also do the following:

    Messages.CUSTOM.format("&6*** LOTTERY *** &r %s").send("This goes to all players on the server!")

You can also provide custom data to your messages:

    Messages.SHOP.provide("price", 10).provide("currency", Material.EMERALD).send("This item cost {price} {currency}").

## The Respawn Game API
Wow, we finally have a central library to handle all your game codes - How awesome is that?
To create a plugin contains a MiniGame, just extend the GamePlugin class, instead of ``Plugin`` or ``JavaPlugin``.
We're now ready to create a game, so let's create a simple class that extends Game, something like this:

    public class MyGame extends Game<GamePlayer, Plugin> {

        public MyGame(Plugin plugin) {
            super(plugin);
        }

        @Override
        protected void addStates() {

        }

        @Override
        protected void addModules() {

        }

        @Override
        protected void onStartGame() {

        }

        @Override
        protected void onEndGame(boolean forcedStop) {

        }

        @NotNull
        @Override
        protected GamePlayer createPlayer(Player player) {
            return new GamePlayer(this, player);
        }

        @Override
        protected GamePlayer[] convertPlayerArray(Collection<GamePlayer> collection) {
            return collection.toArray(new GamePlayer[collection.size()]);
        }

    }

Tip: If you need a custom game player (to add custom fields and methods), just extends the regular GamePlayer class and change
the creation methods in your game class accordingly.

To start your game all you need to do is executing the ``startGame()`` function.

### Game states
You might have used an enum prior to this, we decided to use the as classes this time. Why? Because it's more dynamic
and easier to extend.

We already prepared two game states for you, one prepare game state and one for the in-game stuff. Just extend the
``GameState`` class and add it to the ``addStates`` method like this:

    @Override
    protected void addStates() {
        addState(new MyGameState(this));
    }

To switch to the next state you just have to call ``nextState()``.

If you want to check the current game state you can use something like this:

    if (getGame().getCurrentState() instanceof MyGameState) {
        // The answer is 42...
    }

When the last state is reached, the end will game normally. You can always force-quit the game by calling
``stopGame()``.

### Game modules
Game modules extend the normal game functionality. Instead of adding new listeners in your plugin class, you can just do
that in a module. **If a _GameModule_ implements the _Listener_ interface its events get automatically registered!**

The Display name of a module is the human-readable name as displayed in the server logs. The "name" of the module
defines the name of the configuration section that every module can have.

And hey, look at that! We even prepared some modules!

#### The Y-Action module
Whenever you need something under a certain y action (global) you can just add this module to your project. To override
the normal behavior, just override the ``onDoAction()`` method.

#### The Gift module
If you want to give some item gifts to your players on a frequent basis, you just need to add this module and that's it!
