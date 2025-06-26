rm -rf application

mvn clean verify

java -Djarmode=tools -jar target/agenda.jar extract --destination application

cd  application

#java --enable-preview -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -Dspring.context.exit=onRefresh -jar agenda.jar
#
#java --enable-preview -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -jar agenda.jar

java --enable-preview -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar agenda.jar

java --enable-preview -XX:AOTCache=app.aot -Dtodo.file=../src/main/resources/tasks/simple.csv -Durl.file=../src/main/resources/tasks/url.csv -Dmix.file=../src/main/resources/tasks/mix.csv -jar agenda.jar
