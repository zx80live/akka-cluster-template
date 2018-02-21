package com.example.util

object ConsoleUtil {

  implicit class StringExt(str: String) {
    def attr(color: String): String = color + str + Console.RESET
  }

}
