FROM openjdk:8
COPY ./target/rpc-demo-1.1-SNAPSHOT.jar /tmp/rpc-demo.jar
WORKDIR /tmp
#EXPOSE 8080
CMD ["java","-jar","rpc-demo.jar"]