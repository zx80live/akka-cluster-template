Akka cluster example for local and EC2 testing
==============================================================
Version 1.0

Description
-----------------------------
```
Master ------> Worker0
           --> Worker1
            ...
           --> WorkerN

```

Master and WorkerX are just cluster's nodes. Each node prints cluster state each 1 minute.
The Master sends some DebugEvent to cluster nodes via some balancer.

Requirements
-----------------------------
- Scala: 2.11.8
- JRE:   8

Local testing
-----------------------------

**1**) Set static mode at `core/main/resources/application.conf`:

```
aws.cluster.staticConfig = true
```

**2**) Set current and sibling's IP and ports at `<module>/core/main/resources/application.conf`

**3**) Start master module

```
$ sbt
> project master
> container:start
```

**4**) Start worker module

```
$ sbt
> project worker
> container:start
```

See below screenshot and comments for the local testing:
![Snapshot](https://raw.githubusercontent.com/zx80live/akka-cluster-template/master/master_worker_example.png)


EC2 testing
-----------------------------
**1**) Set dynamic mode at `core/main/resources/application.conf`:
```
aws.cluster.staticConfig = false
```

**2**) Deploy to EC2/dockers

**3**) See logs


Useful links:
-----------------------------
https://github.com/chrisloy/akka-ec2
https://medium.com/@ukayani/deploying-clustered-akka-applications-on-amazon-ecs-fbcca762a44c

Issues:
-----------------------------
http://grokbase.com/t/gg/akka-user/153s1neq95/2-3-9-cluster-unstable-on-ec2
https://github.com/akka/akka/issues/18474
https://github.com/akka/akka/issues/23754
https://stackoverflow.com/questions/48628656/running-an-akka-cluster-with-ec2-and-docker-nodes-arent-registered-in-akka-clu
