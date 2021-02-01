# GeoServer Java REST client

## Run integration tests:

```
mvn verify -Dfailsafe.skipTests=false
```

Will fire up the geoserver docker container at
pre-integration-tests, run the tests at
integration-test, and shut the container down
at post-integration-tests.

## Run from IDE:

Have the GeoServer docker container running before executing
the tests from the IDE:

```
docker run -it --rm --name gstests -p8080:8080 oscarfonts/geoserver:2.15.4
```

Pass the following environment variable to the test run configuration
on the IDE:

```
-Dgeoserver_api_url=http://localhost:8080/geoserver/rest
```

