## Prerequisites

* [Maven 3.0.*](http://maven.apache.org/download.cgi)
* [Heroku Toolbelt](https://toolbelt.heroku.com/)
* Git
* RabbitMQ: `brew install rabbitmq`

## Install ETL job code

    mvn install:install-file \
        -DgroupId=com.delvepartners.etl \
        -DartifactId=test-job \
        -Dversion=1.0 \
        -Dpackaging=jar \
        -Dfile=<path-to-test-job-jar> \
        -DlocalRepositoryPath=lib \
        -DcreateChecksum=true \
        -DgeneratePom=true

## Run in development environment

    # confirm MQ URL in ${basedir}/.env
    mvn package
    foreman start

## Run on Heroku

    heroku create
    heroku addons:add cloudamqp
    git push heroku master
    heroku scale scheduler=1
    heroku scale worker=2
    heroku logs -t
