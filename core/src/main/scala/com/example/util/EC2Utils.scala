package com.example.util

import java.net.{InetAddress, NetworkInterface}

import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.ec2.AmazonEC2Client
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.util.Try

object EC2Utils {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  lazy val ec2 = {
    val credentials = new InstanceProfileCredentialsProvider
    val region = Region.getRegion(Regions.US_EAST_1)
    val scalingClient = new AmazonAutoScalingClient(credentials) {
      setRegion(region)
    }

    val ec2Client = new AmazonEC2Client(credentials) {
      setRegion(region)
    }
    new EC2(scalingClient, ec2Client)
  }

  @deprecated("For testing only. Use dynamic config for testing on EC2")
  def staticConfig: ClusterConfig = {
    ClusterConfig(
      current = NodeConfig(
        ip = ConfigUtils.getProperty("aws.cluster.current.ip"),
        port = ConfigUtils.getProperty("aws.cluster.current.port"),
        bindHostname = ConfigUtils.getPropertyOpt("akka.remote.netty.tcp.bind-hostname").getOrElse(""),
        bindPort = ConfigUtils.getPropertyOpt("akka.remote.netty.tcp.bind-port").getOrElse("")
      ),
      siblings = ConfigUtils.configFactory.getStringList("aws.cluster.siblings.ip").toList.flatMap { c =>
        List(
          NodeConfig(c, ClusterUtils.masterPort), // for master node
          NodeConfig(c, ClusterUtils.clientPort), // for cluster client
          NodeConfig(c, ClusterUtils.workerPort) // for worker node
          // reserved nodes on the same host
        )
      }
    )
  }

  def dynamicConfig(port: String, bindPort: String, bindHostOpt: Option[String] = None): ClusterConfig = {

    val ec2instance = ec2
    val inst = ec2instance.currentInstance
    val bindHost = bindHostOpt.getOrElse("0.0.0.0") //bindHostOpt.getOrElse(Try(InetAddress.getLocalHost.getHostAddress).getOrElse("0.0.0.0")) // this doesn't work, use "0.0.0.0" only
    logger.info("---[EC2.info]----------------")
    logger.info(s"ec2instance.currentIp=${Try(ec2instance.currentIp)}")
    logger.info(s"ec2instance.siblingIps=${Try(ec2instance.siblingIps)}")
    logger.info(s"ec2instance.getPublicIpAddress=${Try(inst.getPublicIpAddress)}")
    logger.info(s"ec2instance.getPrivateIpAddress=${Try(inst.getPrivateIpAddress)}")
    logger.info(s"NetworkInterface.getLocalHost=${Try(NetworkInterface.getNetworkInterfaces.toList)}")
    logger.info(s"InetAddress.getLocalHost=${Try(InetAddress.getLocalHost)}")
    logger.info(s"bindHost=$bindHost")

    logger.info("-----------------------------")

    ClusterConfig(
      current = NodeConfig(
        ip = ec2instance.currentIp,
        port = port,
        bindHostname = bindHost,
        bindPort = bindPort
      ),
      siblings = ec2instance.siblingIps.flatMap { c =>
        List(
          NodeConfig(c, ClusterUtils.masterPort), // for master node
          //NodeConfig(c, ClusterHelper.defaultClientPort), // for cluster client
          NodeConfig(c, ClusterUtils.workerPort) // for worker node
          // reserved nodes on the same host
        )
      }
    )
  }

  def currentConfig(port: String, bindPort: String, bindHost: Option[String] = None): ClusterConfig = {
    val isStaticConfig = ConfigUtils.getPropertyOpt("aws.cluster.staticConfig").forall(_.toBoolean)
    logger.info(s"isStaticConfig = $isStaticConfig")

    if (isStaticConfig)
      staticConfig
    else
      dynamicConfig(port, bindPort, bindHost)
  }
}
