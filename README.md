# Install Talend system routines dependency

    mvn install:install-file \
        -DgroupId=org.talend \
        -DartifactId=system-routines \
        -Dversion=5.2.2 \
        -Dpackaging=jar \
        -Dfile=<path-to-system-routines-jar> \
        -DlocalRepositoryPath=lib \
        -DcreateChecksum=true \
        -DgeneratePom=true

# Install Delve Partners ETL routines dependency

    mvn install:install-file \
        -DgroupId=com.delvepartners.db \
        -DartifactId=etl \
        -Dversion=1.0 \
        -Dpackaging=jar \
        -Dfile=<path-to-etl-jar> \
        -DlocalRepositoryPath=lib \
        -DcreateChecksum=true \
        -DgeneratePom=true

# Install ETL job code

    mvn install:install-file \
        -DgroupId=com.delvepartners.etl \
        -DartifactId=test-job \
        -Dversion=1.0 \
        -Dpackaging=jar \
        -Dfile=<path-to-test-job-jar> \
        -DlocalRepositoryPath=lib \
        -DcreateChecksum=true \
        -DgeneratePom=true

# Run on Heroku

    heroku create
    heroku addons:add cloudamqp
    git push heroku master
    heroku scale scheduler=1
    heroku scale worker=2
    heroku logs -t