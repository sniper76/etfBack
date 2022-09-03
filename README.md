#keystore
cd $JAVA_HOME
bin/keytool -list -keystore $JAVA_HOME/lib/security/cacerts
bin/keytool -delete -alias datagokr -keystore $JAVA_HOME/lib/security/cacerts
bin/keytool -importcert -alias datagokr -keystore $JAVA_HOME/lib/security/cacerts -file /c/dev/source/git/etfBack/datagokr.cer

javac InstallCert.java
java -cp ./ InstallCert data.go.kr
$JAVA_HOME/bin/keytool -exportcert -keystore /c/dev/source/jssecacerts -storepass changeit -file /c/dev/source/output.cert -alias data.go.kr-1
$JAVA_HOME/bin/keytool -importcert -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file /c/dev/source/output.cert -alias datagokr-1
$JAVA_HOME/bin/keytool -list -keystore $JAVA_HOME/lib/security/cacerts


##STS
Install JRE check
openjdk17-jre


###Gradle
./gradlew build
cd build/libs/
nohup java -jar RestEtfApi-0.0.1-SNAPSHOT.jar 1> /dev/null 2>&1 &

CURRENT_PID=$(pgrep -f RestEtfApi-0.0.1-SNAPSHOT.jar)
echo "$CURRENT_PID"
kill 6499


####mongodb
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/4.4 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.4.list
sudo apt-get update
sudo apt-get install -y mongodb


ps --no-headers -o comm 1
sudo systemctl start mongodb

IP PORT 변경
vi /etc/mongodb.conf

디비연결
mongo --port 포트번호

use admin
db.createUser( { user: "계정명", pwd: "비밀번호", roles: ["root"] })



iptables 1.8.4
sudo iptables -I INPUT 5 -i ens3 -p tcp --dport 포트번호 -m state --state NEW,ESTABLISHED -j ACCEPT
sudo iptables -I INPUT 5 -i ens3 -p tcp --dport 포트번호 -m state --state NEW,ESTABLISHED -j ACCEPT
sudo iptables -L --line-numbers
iptables-save > /etc/iptables/rules.v4


##### locale
sudo apt-get install language-pack-ko

http://140.238.19.92:8088/api/stockData?id=STK
http://140.238.19.92:8088/api/fnguide?id=STK
http://140.238.19.92:8088/api/krxData?id=STK&dt=20220713


https://www.youtube.com/watch?v=-xqy0oRW0j4
/bin/mount -o remount, rw /
cd /home/ubuntu/.ssh
ls -al
echo '키파일내용' >> authorized_keys

cd /home/opc/.ssh
ls -al
echo '키파일내용' >> authorized_keys

/usr/sbin/reboot -f