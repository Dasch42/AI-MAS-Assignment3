cd %~dp0
del /s /q /f *.class
javac -cp . client/SearchClient.java
java -jar server.jar -l levels/MAallInOne.lvl -g 200 -c "java  -Xmx2048m client.SearchClient"

pause