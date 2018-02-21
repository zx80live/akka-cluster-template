package com.example.util

import java.net.InetAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import com.example.util.ConsoleUtil._
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.util.Try

object ClusterUtils {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val masterPort: String = "2551"
  val workerPort: String = "3000"
  val clientPort: String = "5000"

  lazy val clusterClient: (ActorRef, ActorSystem) = {
    class FrontendClient extends ClusterNode {

      override def receiveEvents: Receive = {
        case cmd =>
          log.info(s"[receiveEvents] $cmd".attr(Console.YELLOW_B).attr(Console.BLUE))
          cluster.state.members.find(_.roles.contains("frontend")).foreach { m =>
            val a = cluster.system.actorSelection(s"${m.address.toString}/user/frontend*")
            a ! cmd
          }
      }
    }

    val system = clusterSystem(clientPort, clientPort, roles = List("client"), Some(InetAddress.getLocalHost.getHostAddress))
    (system.actorOf(Props[FrontendClient], "client"), system)
  }

  def clusterSystem(clusterConfig: ClusterConfig, roles: List[String]): ActorSystem = {
    val seeds = clusterConfig.siblings.map(s => s"akka.tcp://${clusterConfig.name}@${s.ip}:${s.port}")

    val current = ConfigFactory.empty()
      .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(clusterConfig.current.ip))
      .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(clusterConfig.current.port))
      .withValue("akka.cluster.seed-nodes", ConfigValueFactory.fromIterable(seeds))
      .withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(roles))

    val currentWithBinds =
      if (clusterConfig.current.bindHostname.isEmpty) {
        logger.info("using config without binds")
        current
      } else {
        logger.info("using config with binds")
        current
          .withValue("akka.remote.netty.tcp.bind-hostname", ConfigValueFactory.fromAnyRef(clusterConfig.current.bindHostname))
          .withValue("akka.remote.netty.tcp.bind-port", ConfigValueFactory.fromAnyRef(clusterConfig.current.bindPort))
      }

    val config = currentWithBinds withFallback ConfigFactory.load()

    ClusterHelperDebug.log(clusterConfig, roles, seeds)
    ClusterHelperDebug.log(config)

    ActorSystem(clusterConfig.name, config)
  }

  def clusterSystem(port: String, bindPort: String, roles: List[String], bindHost: Option[String] = None): ActorSystem = {
    val config = EC2Utils.currentConfig(port, bindPort, bindHost)
    clusterSystem(config, roles)
  }


  object ClusterHelperDebug {

    def log(config: ClusterConfig, roles: Seq[String], seeds: Seq[String]): Unit = {
      logger.info(Console.BLUE + "---[ClusterConfig]-------------" + Console.RESET)
      logger.info(s"current=${config.current}")
      logger.info(s"name=${config.name}")
      config.siblings foreach (s => logger.info(s"$s"))
      logger.info(s"roles=$roles")
      logger.info(s"seeds:")
      seeds foreach (s => logger.info(s" $s"))
      logger.info(Console.BLUE + "-------------------------------" + Console.RESET)
    }

    def log(config: Config): Unit = {
      logger.info(Console.GREEN + "---[ActorSystemConfig]-------------" + Console.RESET)
      logger.info(s"""akka.remote.netty.tcp.hostname=${Try(config.getString("akka.remote.netty.tcp.hostname"))}""")
      logger.info(s"""akka.remote.netty.tcp.port=${Try(config.getString("akka.remote.netty.tcp.port"))}""")
      logger.info(s"""akka.cluster.roles=${Try(config.getStringList("akka.cluster.roles"))}""")
      logger.info(s"""akka.remote.netty.tcp.bind-hostname=${Try(config.getString("akka.remote.netty.tcp.bind-hostname"))}""")
      logger.info(s"""akka.remote.netty.tcp.bind-port=${Try(config.getString("akka.remote.netty.tcp.bind-port"))}""")
      logger.info(s"""akka.cluster.seed-nodes=""")
      config.getStringList("akka.cluster.seed-nodes") foreach (s => logger.info(s" $s"))
      logger.info(Console.GREEN + "-----------------------------------" + Console.RESET)
    }
  }

}

case class NodeConfig(ip: String, port: String, bindHostname: String = "", bindPort: String = "")

case class ClusterConfig(current: NodeConfig, siblings: List[NodeConfig], name: String = "ClusterSystem")