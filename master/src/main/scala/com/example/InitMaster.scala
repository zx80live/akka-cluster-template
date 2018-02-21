package com.example

import javax.servlet.{ServletContextEvent, ServletContextListener}

import akka.actor.{ActorSystem, Props}
import com.example.util.ClusterUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.language.postfixOps

class InitMaster extends ServletContextListener {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  lazy val system: ActorSystem = ClusterUtils.clusterSystem(
    port = ClusterUtils.masterPort,
    bindPort = ClusterUtils.masterPort,
    roles = List("frontend"))

  lazy val ctx@(c, clientSystem) = ClusterUtils.clusterClient

  override def contextInitialized(e: ServletContextEvent): Unit = {
    val m = system.actorOf(Props[MasterActor], s"frontend")
    //ctx

    implicit val ec: ExecutionContextExecutor = system.dispatcher
    //system.scheduler.schedule(45 seconds, 45 seconds, c, s"DebugEvent-${System.currentTimeMillis()}")
    system.scheduler.schedule(45 seconds, 45 seconds, m, s"DebugEvent-${System.currentTimeMillis()}")
  }


  override def contextDestroyed(e: ServletContextEvent): Unit = {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    Await.result(clientSystem.terminate(), Duration.Inf)
    Await.result(system.terminate(), Duration.Inf)
  }
}