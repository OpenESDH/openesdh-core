package dk.openesdh.repo.services.contacts

import com.typesafe.scalalogging.slf4j.StrictLogging
import dk.openesdh.repo.model.OpenESDHModel
import org.alfresco.error.AlfrescoRuntimeException
import org.alfresco.model.ContentModel
import org.alfresco.repo.cache.{SimpleCache}
import org.alfresco.repo.model.Repository
import org.alfresco.repo.node.SystemNodeUtils
import org.alfresco.repo.security.authority.AuthorityDAOImpl
import org.alfresco.repo.tenant.TenantService
import org.alfresco.service.cmr.repository.{NodeRef, NodeService}
import org.alfresco.service.namespace.{NamespacePrefixResolver, NamespaceService, QName}
import org.apache.commons.codec.digest.DigestUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.Set

/**
 * @author Lanre Abiwon.
 */
class ContactDAOImpl (val nodeService: NodeService, val namespacePrefixResolver: NamespacePrefixResolver, val singletonCache:SimpleCache[String, Object],
                      val repositoryHelper: Repository, val tenantService: TenantService, val namespaceService: NamespaceService,
                      val zoneAuthorityCache: SimpleCache[Pair[String, String], NodeRef], val authorityLookupCache: SimpleCache[Pair[String, String], NodeRef]
                     ) extends AuthorityDAOImpl with StrictLogging with Serializable{

  private val KEY_SYSTEMCONTAINER_NODEREF: String = "key.systemcontainer.noderef"
  private val name:String = "sys:contacts"
  private val qnameAssocContainers = QName.createQName("sys", "contacts", namespacePrefixResolver)


  def createContact(email: String, contactType: String, authorityZones: scala.collection.immutable.Set[String], aspectProps: Map[QName, String]) : NodeRef = {
    val props: Map[QName, String] = Map(ContentModel.PROP_NAME -> DigestUtils.md5Hex(email), ContentModel.PROP_AUTHORITY_NAME -> email,
      ContentModel.PROP_AUTHORITY_DISPLAY_NAME -> email, OpenESDHModel.PROP_CONTACT_EMAIL -> email, OpenESDHModel.PROP_CONTACT_TYPE -> contactType
    )
    //var childRef: NodeRef = null
    val contactContainerRef: NodeRef = getSystemContainer(qnameAssocContainers)
    val childAssocRef = this.nodeService.createNode(contactContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", email, namespacePrefixResolver), OpenESDHModel.TYPE_CONTACT_BASE, props)

    if (authorityZones != null) {
      val zoneRefs: scala.collection.mutable.Set[NodeRef] = Set()
      val currentUserDomain: String = tenantService.getCurrentUserDomain
      for (authorityZone <- authorityZones) {
        zoneRefs.add(getOrCreateZone(authorityZone))
         zoneAuthorityCache.remove((currentUserDomain, authorityZone))
      }
      zoneAuthorityCache.remove(new Pair[String, String](currentUserDomain, null))
      nodeService.addChild(zoneRefs, childAssocRef.getChildRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("cm", email, namespacePrefixResolver))
    }
//    authorityLookupCache.put(cacheKey(name), childRef)

    childAssocRef.getChildRef
  }


  def getSystemContainer(assocQName: QName): NodeRef = {
    val cacheKey: String = KEY_SYSTEMCONTAINER_NODEREF + "." + assocQName.toString
    var systemContainerRef: NodeRef = singletonCache.get(cacheKey).asInstanceOf[NodeRef]

    if (systemContainerRef == null) {
      val contactsrootNode = SystemNodeUtils.getOrCreateSystemChildContainer(getContainerQName(), nodeService, repositoryHelper).getFirst
      if (contactsrootNode == null) {
        throw new AlfrescoRuntimeException("Required system container for storing contacts not found: ")
      }

      systemContainerRef = contactsrootNode
      singletonCache.put(cacheKey, contactsrootNode)
    }

    systemContainerRef
  }

  def getContainer():NodeRef =  {
    SystemNodeUtils.getSystemChildContainer(getContainerQName(), nodeService, repositoryHelper);
  }

  def getContainerQName():QName = {
    val container: QName = QName.createQName(name, namespaceService)
    container
  }
}
