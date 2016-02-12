package dk.openesdh.repo.classification.sync.kle;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dk.klexml.emneplan.EmneKomponent;
import dk.klexml.emneplan.GruppeKomponent;
import dk.klexml.emneplan.HovedgruppeKomponent;
import dk.klexml.facetter.HandlingsfacetKategoriKomponent;
import dk.klexml.facetter.HandlingsfacetKomponent;

import dk.openesdh.repo.classification.sync.ClassificationSynchronizer;
import dk.openesdh.repo.utils.ClassPathURLHandler;

/**
 * Created by syastrov on 6/1/15.
 */
public class KLEClassificationSynchronizer extends AbstractLifecycleBean implements ClassificationSynchronizer {

    private static final Log logger = LogFactory.getLog(KLEClassificationSynchronizer.class);

    private TransactionService transactionService;
    private CategoryService categoryService;
    private String kleEmneplanURL;
    private String kleFacetterURL;
    private Repository repositoryHelper;
    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;
    private TenantAdminService tenantAdminService;

    private boolean syncOnStartupIfMissing;
    private boolean syncEnabled;

    @Override
    public void synchronize() {
        new Thread(() -> {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            List<String> tenants = getAllTenants();
            tenants.add(TenantService.DEFAULT_DOMAIN);
            synchronizeInternal(tenants);
            AuthenticationUtil.clearCurrentSecurityContext();
        }).start();
    }

    @Override
    public void synchronizeTenant(String tenantDomain) {
        new Thread(() -> {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            synchronizeInternal(Arrays.asList(tenantDomain));
            AuthenticationUtil.clearCurrentSecurityContext();
        }).start();
    }

    /**
     * Return a URL object. Supports URLs starting with "classpath:" to load
     * resources from the classpath.
     *
     * @param url URL
     * @return
     */
    protected URL asURL(String url) throws MalformedURLException {
        if (url.startsWith("classpath:")) {
            return new URL(null, url, new ClassPathURLHandler());
        } else {
            return new URL(url);
        }
    }

    protected void synchronizeInternal() {
        synchronizeInternal(Arrays.asList(TenantService.DEFAULT_DOMAIN));
    }

    protected void synchronizeInternal(List<String> tenants) {
        logger.info("KLE synchronization");
        try {
            logger.info("Loading KLE Emneplan XML file from " + kleEmneplanURL);
            new EmneplanLoader(tenants).load(asURL(kleEmneplanURL).openStream());
            logger.info("Loading KLE Facetter XML file from " + kleFacetterURL);
            new FacetterLoader(tenants).load(asURL(kleFacetterURL).openStream());
        } catch (IOException e) {
            throw new AlfrescoRuntimeException("Error loading KLE emneplan XML file", e);
        } catch (JAXBException | SAXException e) {
            throw new AlfrescoRuntimeException("Error parsing KLE emneplan XML file", e);
        }
    }

    private List<String> getAllTenants() {
        return tenantAdminService.getAllTenants()
                .stream()
                .map(Tenant::getTenantDomain)
                .collect(Collectors.toList());
    }

    /**
     * Get the root category, below cm:generalclassifiable. This avoids
     * using CategoryService's getRootCategories, because it relies on Solr,
     * which is not available during bootstrap.
     *
     * @param rootCategoryName
     * @param create
     * @return
     */
    NodeRef getRootCategory(String rootCategoryName, boolean create) {
        NodeRef rootNode = nodeService.getRootNode(repositoryHelper.getCompanyHome().getStoreRef());
        List<NodeRef> nodeRefs = serviceRegistry.getSearchService().selectNodes(rootNode,
                "/cm:categoryRoot/cm:generalclassifiable",
                null, serviceRegistry.getNamespaceService(), false);
        NodeRef rootCategoryNodeRef = nodeRefs.get(0);
        ChildAssociationRef category = categoryService.getCategory(rootCategoryNodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, rootCategoryName);
        if (category != null) {
            return category.getChildRef();
        } else if (create) {
            return categoryService.createCategory(rootCategoryNodeRef, rootCategoryName);
        } else {
            return null;
        }
    }

    boolean rootCategoryExists(String rootCategoryName) {
        return getRootCategory(rootCategoryName, false) != null;
    }

    NodeRef getOrCreateRootCategory(String rootCategoryName) {
        return getRootCategory(rootCategoryName, true);
    }

    void deleteRootCategoryIfExists(String rootCategoryName) {
        NodeRef rootCategory = getRootCategory(rootCategoryName, false);
        if (rootCategory != null) {
            nodeService.deleteNode(rootCategory);
        }
    }

    NodeRef createOrUpdateCategory(NodeRef parent, String number,
            String title) {
        String name = number;

        ChildAssociationRef childAssoc = categoryService.getCategory(parent, ContentModel.ASPECT_GEN_CLASSIFIABLE, name);
        if (childAssoc != null) {
            // Update existing category metadata
            NodeRef nodeRef = childAssoc.getChildRef();
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            properties.put(ContentModel.PROP_TITLE, title);
            // TODO: Set other metadata
            nodeService.setProperties(nodeRef, properties);
            return childAssoc.getChildRef();
        } else {
            NodeRef category = categoryService.createCategory(parent, name);
            Map<QName, Serializable> aspectProperties = new HashMap<>();
            aspectProperties.put(ContentModel.PROP_TITLE, title);
            // TODO: Set other metadata
            nodeService.addAspect(category, ContentModel.ASPECT_TITLED,
                    aspectProperties);
            return category;
        }
    }

    public class EmneplanLoader {

        private final Log logger = LogFactory.getLog(EmneplanLoader.class);
        private final List<String> tenants;
        private Unmarshaller unmarshaller;

        public final static String ROOT_CATEGORY_NAME = "kle_emneplan";

        protected NodeRef emneplanRootNodeRef;

        EmneplanLoader(List<String> tenants) {
            this.tenants = tenants;
        }

        void load(InputStream is) throws JAXBException, IOException, SAXException {
            this.unmarshaller = JAXBContext.newInstance("dk.klexml.emneplan").createUnmarshaller();
            // Parse the XML document
            logger.info("Parsing KLE Emneplan XML file");
            Document document = XMLUtil.parse(is);
            NodeList childNodes = document.getDocumentElement().getChildNodes();

            for (String tenant : tenants) {
                if (TenantService.DEFAULT_DOMAIN.equals(tenant)) {
                    load(childNodes);
                } else {
                    TenantUtil.runAsTenant(() -> {
                        load(childNodes);
                        return null;
                    }, tenant);
                }
            }
        }

        private void load(NodeList childNodes) throws JAXBException, IOException, SAXException {
            emneplanRootNodeRef = getOrCreateRootCategory(ROOT_CATEGORY_NAME);
            // Unmarshall it in chunks, one Hovedgruppe at a time
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element elem = (Element) node;
                    if (elem.getTagName().equals("Hovedgruppe")) {
                        JAXBElement<HovedgruppeKomponent> hge = unmarshaller.unmarshal(elem, HovedgruppeKomponent.class);
                        processHovedGruppe(hge.getValue());
                    }
                }
            }
        }

        void processHovedGruppe(HovedgruppeKomponent hg) {
            logger.info("Creating category for HovedgruppeNr " + hg.getHovedgruppeNr() + " " + hg.getHovedgruppeTitel());
            NodeRef hovedCategory = createOrUpdateCategory(emneplanRootNodeRef,
                    hg.getHovedgruppeNr(), hg.getHovedgruppeTitel());
            for (GruppeKomponent gruppe : hg.getGruppe()) {
                processGruppe(hovedCategory, gruppe);
            }
        }

        NodeRef processGruppe(NodeRef parent, GruppeKomponent gruppe) {
            NodeRef gruppeCategory = createOrUpdateCategory(parent, gruppe.getGruppeNr(), gruppe.getGruppeTitel());
            for (EmneKomponent emne : gruppe.getEmne()) {
                processEmne(gruppeCategory, emne);
            }
            return gruppeCategory;
        }

        NodeRef processEmne(NodeRef parent, EmneKomponent emne) {
            return createOrUpdateCategory(parent, emne.getEmneNr(), emne.getEmneTitel());
        }

    }

    public class FacetterLoader {

        private final Log logger = LogFactory.getLog(FacetterLoader.class);

        private final List<String> tenants;
        private Unmarshaller unmarshaller;

        public final static String ROOT_CATEGORY_NAME = "kle_facetter";

        protected NodeRef facetterRootNodeRef;

        FacetterLoader(List<String> tenants) {
            this.tenants = tenants;
        }

        void load(InputStream is) throws JAXBException, IOException, SAXException {
            this.unmarshaller = JAXBContext.newInstance("dk.klexml.facetter").createUnmarshaller();
            // Parse the XML document
            logger.info("Parsing KLE Facetter XML file");
            Document document = XMLUtil.parse(is);
            NodeList childNodes = document.getDocumentElement().getChildNodes();

            for (String tenant : tenants) {
                if (TenantService.DEFAULT_DOMAIN.equals(tenant)) {
                    load(childNodes);
                } else {
                    TenantUtil.runAsTenant(() -> {
                        load(childNodes);
                        return null;
                    }, tenant);
                }
            }
        }

        private void load(NodeList childNodes) throws JAXBException, IOException, SAXException {
            facetterRootNodeRef = getOrCreateRootCategory(ROOT_CATEGORY_NAME);
            // Unmarshall it in chunks, one HandlingsfacetKategori at a time
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element elem = (Element) node;
                    if (elem.getTagName().equals("HandlingsfacetKategori")) {
                        JAXBElement<HandlingsfacetKategoriKomponent> hfk = unmarshaller.unmarshal(elem, HandlingsfacetKategoriKomponent.class);
                        processFacetKategori(hfk.getValue());
                    }
                }
            }
        }

        void processFacetKategori(HandlingsfacetKategoriKomponent fk) {
            logger.info("Creating category for HandlingsfacetKategoriNr " + fk.getHandlingsfacetKategoriNr() + " " + fk.getHandlingsfacetKategoriTitel());
            NodeRef hovedCategory = createOrUpdateCategory(facetterRootNodeRef,
                    fk.getHandlingsfacetKategoriNr(), fk.getHandlingsfacetKategoriTitel());
            for (HandlingsfacetKomponent facet : fk.getHandlingsfacet()) {
                processFacet(hovedCategory, facet);
            }
        }

        NodeRef processFacet(NodeRef parent, HandlingsfacetKomponent facet) {
            return createOrUpdateCategory(parent, facet.getHandlingsfacetNr(),
                    facet.getHandlingsfacetTitel());
        }

    }

    public void init() {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "categoryService", categoryService);
        PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "kleEmneplanURL", kleEmneplanURL);
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        if (!syncEnabled || !syncOnStartupIfMissing) {
            return;
        }
        AuthenticationUtil.runAsSystem(() -> {
            // Sync on application startup, if there has never been a sync before
            if (!rootCategoryExists(EmneplanLoader.ROOT_CATEGORY_NAME) || !rootCategoryExists(FacetterLoader.ROOT_CATEGORY_NAME)) {
                logger.info("KLE categories are missing. Performing sync.");
                synchronize();
            }
            return null;
        });
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {

    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setKleEmneplanURL(String kleEmneplanURL) {
        this.kleEmneplanURL = kleEmneplanURL;
    }

    public void setKleFacetterURL(String kleFacetterURL) {
        this.kleFacetterURL = kleFacetterURL;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSyncOnStartupIfMissing(boolean syncOnStartupIfMissing) {
        this.syncOnStartupIfMissing = syncOnStartupIfMissing;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    public void setTenantAdminService(TenantAdminService tenantAdminService) {
        this.tenantAdminService = tenantAdminService;
    }
}
