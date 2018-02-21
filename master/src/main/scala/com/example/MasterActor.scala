package com.example

import akka.actor.ActorRef
import akka.cluster.metrics.{AdaptiveLoadBalancingGroup, HeapMetricsSelector}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import com.example.util.ClusterNode

class MasterActor extends ClusterNode {

  val backend: ActorRef = context.actorOf(
    ClusterRouterGroup(
      AdaptiveLoadBalancingGroup(HeapMetricsSelector),
      ClusterRouterGroupSettings(
        totalInstances = 100, routeesPaths = List(s"/user/worker*"),
        allowLocalRoutees = true, useRoles = Set("worker"))).props(),
    name = "workerRouter2")

  //  val backend: ActorRef = context.actorOf(
  //    ClusterRouterGroup(
  //      RoundRobinGroup(Nil),
  //      ClusterRouterGroupSettings(
  //        totalInstances = 100,
  //        routeesPaths = List("/user/worker*"),
  //        allowLocalRoutees = false,
  //        useRoles = Set("worker")
  //      )).props(),
  //    name = "workerRouter")

  override def receiveEvents: Receive = {
    case e: String =>
      log.info(Console.GREEN + s"handle: $e" + Console.RESET)
      backend ! e

    case msg =>
      log.info(Console.RED + s"unknown event: $msg" + Console.RESET)
  }
}
