FROM alpine as unzipper
WORKDIR /unzip
RUN apk add -U unzip && rm -rf /var/cache/apk/*
ADD target/universal/application.zip /unzip
RUN unzip /unzip/application.zip


FROM phusion/baseimage

CMD ["/sbin/my_init"]

#install JDK
RUN apt update && apt install -y --no-install-recommends \
    default-jre \
    && apt clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=unzipper /unzip/application/ /app/