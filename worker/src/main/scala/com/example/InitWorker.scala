package com.example

import javax.servlet.{ServletContextEvent, ServletContextListener}

import akka.actor.{ActorSystem, Props}
import com.example.util.ClusterUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.language.postfixOps

class InitWorker extends ServletContextListener {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  lazy val system: ActorSystem = ClusterUtils.clusterSystem(
    port = ClusterUtils.workerPort,
    bindPort = ClusterUtils.workerPort,
    roles = List("worker"))

  override def contextInitialized(e: ServletContextEvent): Unit = {
    system.actorOf(Props[WorkerActor], "worker")
  }

  override def contextDestroyed(e: ServletContextEvent): Unit = {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    Await.result(system.terminate(), Duration.Inf)
  }
}