FROM ubuntu
RUN apt update -y
RUN apt install openjdk-21-jdk -y
RUN java -version
COPY ./target/students-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java","-jar"]
CMD ["students-0.0.1-SNAPSHOT.jar"]
