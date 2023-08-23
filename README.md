I put this demo together to file a bug with the hibernate team.

This demonstrates an issue with Oracle and Hibernate when using a Timestamp column.
The issue appears if the JVM is not running in UTC timezone and the column does not have timezone information.

There are two unit-tests in `OracleHibernateIssueApplicationTests` which should both succeed, but only one does.
If replacing Oracle with an H2 in Oracle mode, both tests succeed.

The tests store one entity with a timezone column set to 10:00.
Then we search for all entities with time < 09:00.
We should never find anything, but if the systems timezone is set to Europe/Berlin we find an entity.

They can be run with `./gradlew test`.

Setting `hibernate.jdbc.time_zone` to UTC or Europe/Berlin does not make any difference.

To run the test on an arm mac use these steps, as the oracle docker container does not start without these extra steps.

```shell
 colima start --arch x86_64 --memory 4
 docker context use colima
 export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
 export DOCKER_HOST="unix://${HOME}/.colima/docker.sock"
 ./gradlew test
```

To reuse the oracle container between test runs activate the reuse feature

`echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties`