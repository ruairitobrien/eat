package com.emc.gs.eat.host

/**
 * The model for a host. A host is defined by the input provided to the application and will consist or at least
 * the host IP address.
 *
 * @param address the ip address of the host machine to connect to
 */
case class Host(address: String)
