#!/bin/bash
# Script để thiết lập Java 21 cho project này

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Java version:"
java -version

echo ""
echo "Maven version:"
mvn -version

echo ""
echo "JAVA_HOME đã được thiết lập thành: $JAVA_HOME"
echo "Bây giờ bạn có thể chạy: mvn clean compile hoặc mvn spring-boot:run"

