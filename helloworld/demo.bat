set root=C:\Corda_Projects\corda-helloworld\kotlin-source\build\nodes

::cd /D %root%\Simon
::start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

::cd /D %root%\Qantas
::start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

::cd /D %root%\CBA
::start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

::cd /D %root%\Munesh
::start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar


cd /D %root%\Regulatory
start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

cd /D %root%\PartyA
start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

cd /D %root%\PartyB
start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

cd /D %root%\PartyC
start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

cd /D %root%\controller
start cmd /k  java -jar -Xms512m -Xmx1024m corda.jar

cd /D %root%\Regulatory
start cmd /k  java -jar corda-webserver.jar

cd /D %root%\PartyA
start cmd /k  java -jar corda-webserver.jar

cd /D %root%\PartyB
start cmd /k  java -jar corda-webserver.jar

cd /D %root%\PartyC
start cmd /k  java -jar corda-webserver.jar

::cd /D %root%\Simon
::start cmd /k  java -jar corda-webserver.jar

::cd /D %root%\Qantas
::start cmd /k  java -jar corda-webserver.jar

::cd /D %root%\CBA
::start cmd /k  java -jar corda-webserver.jar

::cd /D %root%\Munesh
::start cmd /k  java -jar corda-webserver.jar