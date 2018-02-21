package com.example

import com.example.util.ClusterNode
import com.example.util.ConsoleUtil._

class WorkerActor extends ClusterNode {

  override def receiveEvents: Receive = {
    case e: String =>
      log.info(s"handle: $e".attr(Console.GREEN))

    case unknown =>
      log.info(s"unknown event: $unknown".attr(Console.RED))
  }
}