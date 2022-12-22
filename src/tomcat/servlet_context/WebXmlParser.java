package tomcat.servlet_context;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class WebXmlParser {
    private static final String WEB_XML_URL = "/main/webapp/WEB-INF/web.xml";
    private static final List<String> VALID_TAGS = List.of("filter", "filter-mapping", "servlet", "servlet-mapping");
    private static final Set<String> ORDERED_VALID_TAGS;

    static {
        ORDERED_VALID_TAGS = new LinkedHashSet<>();
        ORDERED_VALID_TAGS.addAll(VALID_TAGS);
    }

    private final ServletContext servletContext;
    private final String webRoot;
    private final String webAppDirFromRoot;

    public WebXmlParser(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webRoot = servletContext.webRoot;
        this.webAppDirFromRoot = webRoot.substring(4).replace("/", ".");
    }

    public void parseWebXML() throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        String fullWebXmlPath = webRoot + "/" + WEB_XML_URL;
        Document document = docBuilder.parse(fullWebXmlPath);
        invalidateWebXML(document);
        invalidateNodes(document);
        //createFilters(document);
        createServlets(document);
        createServletMappings(document);
        //createFilterMappings(document);
    }

    private void invalidateWebXML(Document document) throws IOException {
        NodeList nodeList = document.getChildNodes();
        if (nodeList.getLength() != 2) {
            throw new IOException("invalid web.xml");
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!node.getNodeName().equals("web-app")) {
                throw new IOException("invalid web.xml");
            }
        }
    }

    private void invalidateNodes(Document document) throws IOException {
        Node webAppNode = document.getElementsByTagName("web-app").item(0);
        NodeList nodes = webAppNode.getChildNodes();
        Set<String> nodeNames = new LinkedHashSet<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            if (!nodeName.equals("#text")) {
                nodeNames.add(node.getNodeName());
            }
        }

        if (!nodeNames.equals(ORDERED_VALID_TAGS)) {
            throw new IOException("Invalid order of tags: " + nodeNames);
        }
    }

    private void createServlets(Document document) throws IOException {
        Set<String> servletNames = new LinkedHashSet<>();
        Set<Class<?>> servletClasses = new LinkedHashSet<>();
        parseServlets(document, servletNames, servletClasses);

        Iterator<String> namesIt = servletNames.iterator();
        Iterator<Class<?>> classesIt = servletClasses.iterator();
        while (namesIt.hasNext() && classesIt.hasNext()) {
            servletContext.addServlet(namesIt.next(), classesIt.next());
        }
    }

    private void parseServlets(Document document, Set<String> servletNames, Set<Class<?>> servletClasses) throws IOException {
        NodeList servletNodes = document.getElementsByTagName("servlet");
        for (int i = 0; i < servletNodes.getLength(); i++) {
            Node servletNode = servletNodes.item(i);
            NodeList servletAttr = servletNode.getChildNodes();
            for (int j = 0; j < servletAttr.getLength(); j++) {
                Node attrNode = servletAttr.item(j);

                if (attrNode instanceof Text) {
                    continue;
                }

                String attrName = attrNode.getNodeName();
                switch (attrName) {
                    case "servlet-name" -> addServletName(attrNode, servletNames);
                    case "servlet-class" -> addServletClass(attrNode, servletClasses);
                    default -> throw new IOException("Invalid node: " + attrName);
                }
            }
        }
    }

    private void addServletName(Node attrNode, Set<String> names) throws IOException {
        String servletName = attrNode.getTextContent();
        if (!names.add(servletName)) {
            throw new IOException("Servlet with this name already exists: " + servletName);
        }
    }

    private void addServletClass(Node attrNode, Set<Class<?>> classes) throws IOException {
        String servletClassName = attrNode.getTextContent();
        servletClassName = webAppDirFromRoot + "." + servletClassName;
        try {
            Class<?> servletClass = Class.forName(servletClassName);
            if (!classes.add(servletClass)) {
                throw new IOException("Class already exists: " + servletClass);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found: " + servletClassName);
        }
    }

    private void createServletMappings(Document document) throws IOException {
        List<String> servletNames = new ArrayList<>();
        List<String> urlPatterns = new ArrayList<>();
        parseServletMappings(document, servletNames, urlPatterns);

        for (int i = 0; i < servletNames.size(); i++) {
            String servletName = servletNames.get(i);
            String urlPattern = urlPatterns.get(i);
            servletContext.addServletMapping(servletName, urlPattern);
        }
    }

    private void parseServletMappings(Document document, List<String> servletNames, List<String> urlPatterns) throws IOException {
        NodeList servletNodes = document.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < servletNodes.getLength(); i++) {
            Node servletNode = servletNodes.item(i);
            NodeList servletAttr = servletNode.getChildNodes();
            for (int j = 0; j < servletAttr.getLength(); j++) {
                Node attrNode = servletAttr.item(j);

                if (attrNode instanceof Text) {
                    continue;
                }

                String attrName = attrNode.getNodeName();
                switch (attrName) {
                    case "servlet-name" -> addServletNameInMapping(attrNode, servletNames);
                    case "url-pattern" -> addUrlPattern(attrNode, urlPatterns);
                    default -> throw new IOException("Invalid node: " + attrName);
                }
            }
        }
    }

    private void addUrlPattern(Node attrNode, List<String> urlPatterns) {
        String urlPattern = attrNode.getTextContent();
        urlPatterns.add(urlPattern);
    }

    private void addServletNameInMapping(Node attrNode, List<String> servletNames) throws IOException {
        String servletName = attrNode.getTextContent();
        if (!servletContext.getServlets().containsKey(servletName)) {
            throw new IOException("Servlet with this name doesn't exists: " + servletName);
        }
        servletNames.add(servletName);
    }
}
