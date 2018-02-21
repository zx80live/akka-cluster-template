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

Master and WorkerX are just cluster's nodes. Each node print cluster state each 1 minute.
Master send some DebugEvent to the cluster nodes via some balancer.

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
