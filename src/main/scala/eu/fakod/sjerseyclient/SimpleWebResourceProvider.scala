package eu.fakod.sjerseyclient

import javax.ws.rs.core.UriBuilder
import com.sun.jersey.api.client.filter.LoggingFilter
import com.sun.jersey.client.apache.ApacheHttpClient
import com.sun.jersey.client.apache.config.{ApacheHttpClientConfig, DefaultApacheHttpClientConfig}
import eu.fakod.sjerseyclient.providers.JacksonProvider

/**
 * provider trait for the instance of WebResource used in class rest
 *
 * @author Christopher Schmidt
 */
trait SimpleWebResourceProvider {

  /**
   * client configuration parameter like
   * (ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, java.lang.Boolean.TRUE)
   * (ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, java.lang.Boolean.TRUE)
   */
  protected def getApacheHttpClientConfig: List[(String, AnyRef)] = Nil

  /**
   * allows some custom configuration
   * called after getApacheHttpClientConfig config and
   * before creation of the client
   */
  protected def doConfig(c: DefaultApacheHttpClientConfig): Unit = {}

  /**
   * has to be implemented to return the base URI (host, port, path) as String
   */
  protected def baseUriAsString: String

  /**
   * has to be overwritten so disable the HTTP logging filter
   */
  protected def enableLogFilter = true

  private val baseUri = UriBuilder.fromUri(baseUriAsString).build()

  /**
   * allows to add some Classes to be added to configuration by config.getClasses.add
   */
  protected def addClasses: List[Class[_]] = Nil


  /**
   * creating the configuration for apache client
   * basic authentication and timeouts
   */
  private val config = new DefaultApacheHttpClientConfig()

  /**
   * adding the Case Class Provider
   */
  config.getClasses.add(classOf[JacksonProvider[_]])
  addClasses.foreach(config.getClasses.add(_))

  /**
   * setting up Apache client parameter
   */
  getApacheHttpClientConfig.foreach {
    p =>
      val (k, v) = p
      config.getProperties.put(k, v)
  }

  doConfig(config)

  /**
   * lazy because of configuring with config
   */
  private lazy val client = {
    val c = ApacheHttpClient.create(config)
    doConfig(c)
    c
  }

  /**
   * allows of some custom configuration of client itself
   * where config customization is not enough
   * called after creation of the client and
   * before its first usage
   */
  protected def doConfig(client: ApacheHttpClient): Unit = {}

  /**
   * instance of WebResource created with baseUri
   */
  protected val webResource = client resource baseUri


  if (enableLogFilter)
    client.addFilter(new LoggingFilter())

  /**
   *    creates a new WebResource from an absolute URI
   */
  def getWebResourceFromAbsURI(absPath: String) = {
    val uri = UriBuilder.fromUri(absPath).build()
    client resource uri
  }
}