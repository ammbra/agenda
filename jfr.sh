cd  application

java -XX:StartFlightRecording:method-timing=::<clinit>,duration=60s,filename=init.jfr -jar agenda.jar
jfr view method-timing init.jfr

java -XX:StartFlightRecording:method-trace=java.util.HashMap::resize,report-on-exit=method-timing,duration=60s,filename=rec.jfr -jar agenda.jar
jfr print --events jdk.MethodTrace rec.jfr

jfr scrub --exclude-events jdk.InitialSystemProperty rec.jfr scrubbed.jfr

jfr print --exact rec.jfr