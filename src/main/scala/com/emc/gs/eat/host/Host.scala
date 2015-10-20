package com.emc.gs.eat.host

/**
 * The model for a host. A host is defined by the input provided to the application and will consist or at least
 * the host IP address.
 *
 * @param address the ip address of the host machine to connect to
 * @param os the operating system of the host
 * @param username the username for authentication
 * @param password the password for authentication
 */
case class Host(address: String, os: String, username: String = "", password: String = "")

object Host {
  final val ADDRESS_KEY = "address"
  final val OS_KEY = "os"
  final val PROTOCOL_KEY = "protocol"
  final val USERNAME_KEY = "username"
  final val PASSWORD_KEY = "password"
  final val ERROR_KEY = "error"
}
