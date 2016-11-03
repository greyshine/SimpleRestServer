# 
FROM williamyeh/java8:latest

RUN apt-get -y update

RUN mkdir -p /opt/sirese/bin/
RUN mkdir -p /opt/sirese/home/

COPY SiReSe-0.1-SNAPSHOT.jar /opt/sirese/bin/

RUN ls -laR /opt

RUN java -version

RUN uptime >> /opt/sirese/home/start.info

RUN date -R >> /opt/sirese/home/start.info

VOLUME ["/opt/sirese/home/"]

CMD java -jar /opt/sirese/bin/SiReSe-0.1-SNAPSHOT.jar -path /opt/sirese/home/


