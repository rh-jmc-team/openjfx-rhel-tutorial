# OpenJFX Tutorials

[OpenJFX on RHEL]

Since OpenJFX 11 the project has moved to a more open and healthy community development model, which means that binaries are now easy to obtain also as standalone jar files via maven. In some cases it may be necessary to still build your own distribution, so we will explain how to compile it by yourself. This tutorial assumes a current version of RHEL, specifically 8.1, but similar steps can be followed for any supported version of RHEL, CentOS or Fedora, and, in fact, they are valid for almost any operating system with minor adjustments. In this guide we will also assume you have maven and OpenJDK 11 correctly installed, please refer to the online documentation to see how to do this for your particular distribution.

A note about compatibility: OpenJFX is largely backward compatible with LTS versions of OpenJDK with the developers trying to keep compatibility to at least the previous LTS for any given release, which means that the current version of OpenJFX is compatible with OpenJDK 11 and will most likely remain so at least until the next LTS release will be released.

[Community builds]

If using maven is not an option, you may opt for building OpenJFX yourself or for downloading the community builds. The [OpenJFX website](https://openjfx.io/) provides a lot of useful information as well as community builds.

[Use OpenJFX via Maven]

The configuration via Maven is extremely easy since the libraries are distributed from maven central. Assuming you have a maven project you need to add some configuration, here is what's necessary for OpenJFX 14, which was just released:

```
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>14</version>
    </dependency>
</dependencies>
```

This is all you need to enable OpenJFX in your maven based project! 

Given that OpenJFX is a JDK 11+ library, it relies on modules. While it's still possible to bypass the module system, the recommendation is to fully modularise the application, adding a module-info.java to the module root you wish to use OpenJFX on and add the necessary requires, in our case we will use the `javafx.graphics` and `javafx.controls` packages; we will also need to open our application to `javafx.graphics`, this is necessary because OpenJFX will load our application via reflection using the `Application` API:

```
module com.redhat.jfx.tutorials {
    requires javafx.graphics;
    requires javafx.controls;
    opens com.redhat.jfx.tutorials to javafx.graphics;
}
```

If you are using maven from the command line, you will need to make sure it's using an OpenJDK 11:

```
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
```

The pom included in this tutorial also sets the compiler explicitly to this version of OpenJDK, primarely for the benefit of IDEs, otherwise it should be enough to only set the source and target versions to 11.

To compile the basic project we can use the usual maven commands, all the dependencies will be downloaded as expected from maven central, or from any repository you have configured in your main pom:

```
mvn clean package
```

You can consume the library and the resulting applicaiton in a few different ways, let's see some of them next.

[Run directly as a java application and copy all the dependencies]

This is the default way, you will probably have a launcher of some kind. However, to make things easier, we may want to ask maven to include all the necessary libraries in a single place (as an alternative you can use the `maven-assembly-plugin` to create a meta jar with all the dependencies exploded in).

```
<plugin>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/libs</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>

```

In our case we are using a modularised application, so the command line arguments will need reflect it, using the `maven-dependency-plugin` makes this easier becase now everything we need is inside our project build directory:

```
java --module-path target/libs/javafx-base-14.jar:target/libs/javafx-base-14-linux.jar:target/libs/javafx-controls-14.jar:target/libs/javafx-controls-14-linux.jar:target/libs/javafx-controls-14-linux.jar:target/libs/javafx-graphics-14.jar:target/libs/javafx-graphics-14-linux.jar:target/openjfx-tutorials-1.0-SNAPSHOT.jar -m com.redhat.jfx.tutorials/com.redhat.jfx.tutorials.HelloWorldJFX
```

[Create a JLink image]

This will create a fully standalone image with a binary that will execute your application, again with all the necessary dependencies. The maven configuration for this is the following:

```
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.4</version>
    <configuration>
        <stripDebug>true</stripDebug>
        <compress>2</compress>
        <noHeaderFiles>true</noHeaderFiles>
        <noManPages>true</noManPages>
        <launcher>hello-jfx</launcher>
        <jlinkImageName>HelloWorldJFX</jlinkImageName>
        <jlinkZipName>HelloWorldJFXZip</jlinkZipName>
        <mainClass>com.redhat.jfx.tutorials.HelloWorldJFX</mainClass>
    </configuration>
</plugin>
```

From command line then execute:

```
mvn clean package javafx:jlink
```

This will generate all the necessary files according to your configuration, copy the resources and create a standalone minimal JDK. In our tutorial example this is the `tutorial/HelloWorldJFX/` direcory containing an OpenJDK minimal image, you can then launch the application simply by using the generated binary:

```
./target/HelloWorldJFX/bin/hello-jfx
```

[Execute direcly via the maven plugin]

This is most useful for testing, the `javafx-maven-plugin` we just configured for jlink can also be used to run the application directly:

```
mvn clean package javafx:run
```

[Build OpenJFX yourself]

Building OpenJFX is relatively easy, but it will require some tools to be present on your system. Most of them are part of the standard RHEL installation or are available as modules or SCL extras, but some will need to be downloaded and set up before starting.

The instructions are fairly well captured in this document on the OpenJFX wiki [OpenJFX wiki](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX) and there is not much that is RHEL specific, except the name of some packages, but let's see some details.

Mercurial isn't necessary anymore since the project moved to github, so from root (or via sudo as in the example) do this. For RHEL 8:

```
sudo yum install git bison flex gperf pkgconfig gtk2-devel gtk3-devel pango-devel freetype-devel libXtst-devel java-11-openjdk-devel gcc-c++ libstdc++-static
```

For RHEL 7:

```
yum install -y gcc-c++ make autoconf automake libtool glibc-devel libstdc++-static bison flex gperf ksh pkgconfig libpng12-devel libjpeg-devel libxml2-devel libxslt-devel systemd-devel glib2-devel gtk2-devel gtk3-devel libstdc++-static libappindicator-gtk3 libXtst-devel pango-devel freetype-devel perl-JSON-PP perl-Digest-MD5 ruby --enablerepo=rhel-7-server-optional-rpms
```

This will gather some extra dependencies too.

After you have all of the software, you can simply follow the instructions on the OpenJFX wiki page for the next steps. Gradle will automatically download itself but you need to be sure to set up ant first as this may cause the compilation to fail:

```
wget http://archive.apache.org/dist/ant/binaries/apache-ant-1.10.5-bin.tar.bz2
tar xvfj apache-ant-1.10.5-bin.tar.bz2
export PATH=$PWD/apache-ant-1.10.5/bin/:$PATH
```

Then it's really just as simple as typing:

```
sh gradlew sdk
```

Enjoy!
