package dk.openesdh.repo.classification.sync.kle;

import dk.klexml.EmneKomponent;
import dk.klexml.GruppeKomponent;
import dk.klexml.HovedgruppeKomponent;
import dk.openesdh.repo.classification.sync.ClassificationSynchronizer;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.XMLUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by syastrov on 6/1/15.
 */
public class KLEClassificationSynchronizer extends AbstractLifecycleBean implements ClassificationSynchronizer {
    private static final Log logger = LogFactory.getLog(KLEClassificationSynchronizer.class);

    protected TransactionService transactionService;
    protected CategoryService categoryService;
    protected String kleEmneplanURL;
    protected Repository repositoryHelper;
    protected NodeService nodeService;

    public final static String KLE_EMNEPLAN_ROOT_CATEGORY_NAME = "kle_emneplan";

    protected NodeRef emneplanRootNodeRef;
    protected boolean syncOnStartupIfMissing;


    @Override
    public void synchronize() {
        logger.info("KLE synchronization");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(kleEmneplanURL);
        try {
            logger.info("Fetching KLE Emneplan XML file");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new AlfrescoRuntimeException("KLE data is not available from '" +
                        kleEmneplanURL + "'. HTTP Response code: " + statusLine.getStatusCode());
            } else {
                loadEmneplanXML(entity.getContent());
            }
        } catch (IOException e) {
            throw new AlfrescoRuntimeException("Error fetching KLE emneplan XML file", e);
        } catch (JAXBException | SAXException e) {
            throw new AlfrescoRuntimeException("Error parsing KLE emneplan XML file", e);
        }
    }

    boolean rootCategoryExists(String rootCategoryName) {
        return !categoryService.getRootCategories(
                repositoryHelper.getCompanyHome().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE,
                rootCategoryName).isEmpty();
    }

    NodeRef getOrCreateRootCategory(String rootCategoryName) {
        Collection<ChildAssociationRef> rootCategories = categoryService.getRootCategories(
                repositoryHelper.getCompanyHome().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE,
                rootCategoryName, true);
        return rootCategories.iterator().next().getChildRef();
    }

    void deleteRootCategoryIfExists(String rootCategoryName) {
        Collection<ChildAssociationRef> rootCategories = categoryService.getRootCategories(
                repositoryHelper.getCompanyHome().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE,
                rootCategoryName, false);
        if (!rootCategories.isEmpty()) {
            nodeService.deleteNode(rootCategories.iterator().next().getChildRef());
        }
    }

    void loadEmneplanXML(InputStream is) throws JAXBException, IOException, SAXException {
        if (emneplanRootNodeRef == null) {
            emneplanRootNodeRef = getOrCreateRootCategory(KLE_EMNEPLAN_ROOT_CATEGORY_NAME);
        }

        JAXBContext jc = JAXBContext.newInstance("dk.klexml");
        Unmarshaller u = jc.createUnmarshaller();

        // Parse the XML document
        logger.info("Parsing KLE Emneplan XML file");
        Document document = XMLUtil.parse(is);
        NodeList childNodes = document.getDocumentElement().getChildNodes();

        // Unmarshall it in chunks, one Hovedgruppe at a time
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                Element elem = (Element) node;
                if (elem.getTagName().equals("Hovedgruppe")) {
                    JAXBElement<HovedgruppeKomponent> hge = u.unmarshal(elem, HovedgruppeKomponent.class);
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

    NodeRef createOrUpdateCategory(NodeRef parent, String number,
                                   String title) {
        String name = number;

        ChildAssociationRef childAssoc = categoryService.getCategory(parent, ContentModel.ASPECT_GEN_CLASSIFIABLE, name);
        if (childAssoc != null) {
            // Update existing category metadata
            NodeRef nodeRef = childAssoc.getChildRef();
            Map<QName, Serializable> properties = new HashMap<>();
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

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setKleEmneplanURL(String kleEmneplanURL) {
        this.kleEmneplanURL = kleEmneplanURL;
    }

    public void init() {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "categoryService", categoryService);
        PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "kleEmneplanURL", kleEmneplanURL);
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

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        if (!syncOnStartupIfMissing) {
            return;
        }
        AuthenticationUtil.runAsSystem(
            new AuthenticationUtil.RunAsWork<Object>() {
                @Override
                public Object doWork() throws Exception {
                    // Sync on application startup, if there has never been a sync before
                    if (!rootCategoryExists(KLE_EMNEPLAN_ROOT_CATEGORY_NAME)) {
                        logger.info("KLE categories have never been synced. Performing " +
                                "initial sync.");

                        synchronize();
                    }
                    return null;
                }
            }
        );
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {

    }

    public void setSyncOnStartupIfMissing(boolean syncOnStartupIfMissing) {
        this.syncOnStartupIfMissing = syncOnStartupIfMissing;
    }
}
