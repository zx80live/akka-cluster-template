Akka cluster template for local and EC2 testing
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
- Akka: 2.5.9
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

**2**) The following ports should be allowed in EC2 environment: `2551`, `3000`, `5000`. Also a port biding is used in this mode automatically (at [com.example.util.EC2Utils](https://github.com/zx80live/akka-cluster-template/blob/master/core/src/main/scala/com/example/util/EC2Utils.scala#L67)). 

See [Akka behind NAT or in a Docker container](https://doc.akka.io/docs/akka/snapshot/remoting.html?language=scala#akka-behind-nat-or-in-a-docker-container) for more details.

**3**) Deploy to EC2/dockers

**4**) See logs


Useful links
-----------------------------
https://github.com/chrisloy/akka-ec2

https://medium.com/@ukayani/deploying-clustered-akka-applications-on-amazon-ecs-fbcca762a44c

https://github.com/hseeberger/constructr
       
https://github.com/typesafehub/constructr-zookeeper

Issues
-----------------------------
http://grokbase.com/t/gg/akka-user/153s1neq95/2-3-9-cluster-unstable-on-ec2

https://github.com/akka/akka/issues/18474

https://github.com/akka/akka/issues/23754

https://stackoverflow.com/questions/48628656/running-an-akka-cluster-with-ec2-and-docker-nodes-arent-registered-in-akka-clu

https://stackoverflow.com/questions/48924863/akka-cluster-holds-and-keeps-storing-wrong-info-about-membership-on-ec2-docker
