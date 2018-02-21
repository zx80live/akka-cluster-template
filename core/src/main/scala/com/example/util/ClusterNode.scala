package com.example.util

import akka.actor.{Actor, ActorLogging, Cancellable}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.example.util.ClusterNode.Status
import com.example.util.ConsoleUtil._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

abstract class ClusterNode extends Actor with ActorLogging {

  val selfStatusPeriod: FiniteDuration = 45 seconds

  val cluster: Cluster = Cluster(context.system)

  implicit val ec: ExecutionContextExecutor = context.system.dispatcher

  var selfStatus: Cancellable = context.system.scheduler.schedule(selfStatusPeriod, selfStatusPeriod, self, Status(s"self: $this"))

  override def receive: Receive = receiveClusterEvents orElse receiveEvents

  def receiveEvents: Receive

  def receiveClusterEvents: Receive = {
    case Status(from) =>
      log.info(s"ClusterStatus for [$from]:".attr(Console.GREEN_B).attr(Console.BLUE))
      try {
        cluster.state.members.foreach { m =>
          log.info(s"$m".attr(Console.GREEN))
        }
      } catch {
        case err: Throwable =>
          log.error("Can't get cluster status".attr(Console.RED_B), err)
      }

    case MemberRemoved(m, previousStatus) =>
      log.info(s"Member removed $m after $previousStatus".attr(Console.BLUE_B).attr(Console.RED))

    case MemberUp(m) =>
      if (m.address != cluster.selfAddress)
        log.info(s"Register member $m".attr(Console.BLUE_B))
      else
        log.info("SELF".attr(Console.GREEN_B) + s"Register member $m".attr(Console.BLUE_B))

    case e: MemberEvent =>
      log.info("[M]".attr(Console.BLUE_B) + s"$e".attr(Console.BLUE))

    case e: UnreachableMember =>
      log.info("[M]".attr(Console.BLUE_B).attr(Console.RED) + s"$e".attr(Console.BLUE))
  }

  override def preStart(): Unit = {
    log.info(s"[preStart] subscribing cluster events...".attr(Console.BLUE))
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
    cluster.registerOnMemberRemoved {
      log.info(s"[registerOnMemberRemoved] ...".attr(Console.BLUE))
    }
    cluster.registerOnMemberUp {
      log.info(s"[registerOnMemberUp] ...".attr(Console.BLUE))
    }
  }

  override def postStop(): Unit = {
    log.info(s"[postStop] cancel selfStatus ${Try(selfStatus.cancel())}".attr(Console.BLUE))
    log.info(s"[postStop] unsubscribing cluster events...".attr(Console.BLUE))
    cluster.unsubscribe(self)
  }
}


object ClusterNode {

  trait NodeCommand

  case object LeaveAndShutdown extends NodeCommand

  case class Status(from: String) extends NodeCommand

}