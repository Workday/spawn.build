------------------------------------------------------------------------------------------------------------------------
# NOW
------------------------------------------------------------------------------------------------------------------------

* Refactor/Replace Docker okhttp client with Java HTTP Client

* Introduce Debugging support

* TODO: Create issues for the remaining JDK features/options (eg: JMX, Garbage Collection, Logging, Flight Recorder, Early Access, Preview etc)

* FIX! Optimize creating of SpawnAgent for once per Machine instance or once per JVM?
* DESIGN! We really need to sort out the naming of the Producer.  I really should be Publisher!

------------------------------------------------------------------------------------------------------------------------
# NEXT
------------------------------------------------------------------------------------------------------------------------

* Introduce Composition Tests

* FIX! Introduce AddReads option to support representing "--add-reads" (and inheritance)
* FIX! Introduce AddOpens option to support representing "--add-opens" (and inheritance)

eg: to mimic this stuff that's added automatically!

//            JDKOption.of("--add-reads"), JDKOption.of("build.spawn.platform.local.jdk=ALL-UNNAMED"),
//            JDKOption.of("--add-opens"),
//            JDKOption.of("build.spawn.platform.local.jdk/build.spawn.platform.local.jdk=ALL-UNNAMED"),

------------------------------------------------------------------------------------------------------------------------
# LATER
------------------------------------------------------------------------------------------------------------------------

* Introduce spawn-docker-platform
* Introduce spawn-remote-platform
