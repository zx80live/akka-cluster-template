package com.example.util

import com.typesafe.config.ConfigFactory

import scala.util.Try

object ConfigUtils {
  val configFactory = ConfigFactory.load()

  def getProperty(key: String): String = configFactory.getString(key)

  def getPropertyOpt(key: String): Option[String] = Try(configFactory.getString(key)).toOption
}