package dk.openesdh.repo.classification.sync;

import dk.klexml.HovedgruppeKomponent;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by syastrov on 6/1/15.
 */
public class KLEClassificationSynchronizer implements ClassificationSynchronizer {
    private static final Log logger = LogFactory.getLog(KLEClassificationSynchronizer.class);

    private TransactionService transactionService;
    private CategoryService categoryService;
    private String kleEmneplanURL;
    private Repository repositoryHelper;

    public final static String KLE_EMNEPLAN_ROOT_CATEGORY_NAME = "KLE";

    private NodeRef emneplanRootNodeRef;

    @Override
    public void synchronize() {
        logger.info("KLE synchronization");

        if (emneplanRootNodeRef == null) {
            emneplanRootNodeRef = getOrCreateRootCategory(KLE_EMNEPLAN_ROOT_CATEGORY_NAME);
        }

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

    private NodeRef getOrCreateRootCategory(String rootCategoryName) {
        Collection<ChildAssociationRef> rootCategories = categoryService.getRootCategories(
                repositoryHelper.getCompanyHome().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE,
                rootCategoryName, true);
        return rootCategories.iterator().next().getChildRef();
    }

    private void loadEmneplanXML(InputStream is) throws JAXBException, IOException, SAXException {
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

    private void processHovedGruppe(HovedgruppeKomponent hg) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating category for HovedgruppeNr " + hg
                    .getHovedgruppeNr() + " " + hg.getHovedgruppeTitel());
        }
        getOrCreateCategory(emneplanRootNodeRef, hg.getHovedgruppeNr());
    }

    private NodeRef getOrCreateCategory(NodeRef parent, String name) {
        ChildAssociationRef childAssoc = categoryService.getCategory(emneplanRootNodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, name);
        if (childAssoc != null) {
            return childAssoc.getChildRef();
        } else {
            return categoryService.createCategory(emneplanRootNodeRef, name);
        }
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setKleEmneplanURL(String kleEmneplanURL) {
        this.kleEmneplanURL = kleEmneplanURL;
    }

    public void init() {
        PropertyCheck.mandatory(this, "categoryService", categoryService);
        PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "kleEmneplanURL", kleEmneplanURL);
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }
}
