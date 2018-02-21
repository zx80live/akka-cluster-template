package com.example.util

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.autoscaling.model.{DescribeAutoScalingGroupsRequest, DescribeAutoScalingInstancesRequest}
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeInstancesRequest, Instance, InstanceStateName}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
 * This solution was borrowed from https://github.com/chrisloy/akka-ec2/blob/master/src/main/scala/net/chrisloy/akka/EC2.scala
 */
class EC2(scaling: AmazonAutoScalingClient, ec2: AmazonEC2Client) {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val instanceIdURL: String = ConfigUtils.getProperty("aws.instanceIdUrl")

  val isRunning: Instance => Boolean = _.getState.getName == InstanceStateName.Running.toString

  def siblingIps: List[String] = groupInstanceIds(groupName(instanceId)) map instanceFromId collect {
    case instance if isRunning(instance) => instance.getPrivateIpAddress
  }

  def currentIp: String = {
    val instance = instanceFromId(instanceId)
    instance.getPrivateIpAddress
  }

  def currentInstance: Instance = instanceFromId(instanceId)

  private def instanceId: String = {
    val conn = new URL(instanceIdURL).openConnection
    val in = new BufferedReader(new InputStreamReader(conn.getInputStream))
    try in.readLine() finally in.close()
  }

  private def instanceFromId(id: String): Instance = {
    val result = ec2 describeInstances new DescribeInstancesRequest {
      setInstanceIds(id :: Nil)
    }
    result.getReservations.head.getInstances.head
  }

  private def groupName(instanceId: String): String = {
    val result = scaling describeAutoScalingInstances new DescribeAutoScalingInstancesRequest {
      setInstanceIds(instanceId :: Nil)
    }
    result.getAutoScalingInstances.head.getAutoScalingGroupName
  }

  private def groupInstanceIds(groupName: String): List[String] = {
    val result = scaling describeAutoScalingGroups new DescribeAutoScalingGroupsRequest {
      setAutoScalingGroupNames(groupName :: Nil)
    }
    result.getAutoScalingGroups.head.getInstances.toList map (_.getInstanceId)
  }
}
