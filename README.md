# Example Kotlin serialization project for JVM

This project showcases various built-in and custom serialization formats and
test cases for classes that can be serialized into those formats.

Use `./gradlew runApp` to run a self-test showing how classes are serialization into those formats
and back into Kotlin.

# Requirements
- OracleJDK 8(not openjdk or Oracle JDK9)
- redis-server

# ArchLinux Install of JDK
Oracleは別途AURにあるらしいがyaourtより自分でパッケージをビルドしてしまった方がいい
例えば、OracleJDK8はこのように行う
```console
$ git clone https://aur.archlinux.org/jdk8.git
$ cd jdk8
$ makepkg
$ sudo pacman -U  jdk8-8u144-1-x86_64.pkg.tar.xz
$ archlinix-java status
$ sudo archlinux-java set java-8-jdk
```
このようなオペレーションが別途必要

# Run With Argments
GradleでArgmentsを指定する方法がよくわからなかったが、このように実行することで実現できる
```console
$ ./gradlew run -Dexec.args="placeholder arg1 arg2"
```
長くてめんどくさいね
