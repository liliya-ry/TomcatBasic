package tomcat.servlet_context;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class ServletContext {
    private static final List<String> VALID_TAGS = List.of("filter", "filter-mapping", "servlet", "servlet-mapping");
    private static final Set<String> ORDERED_VALID_TAGS;
    String contextPath;
    Map<String, Class<?>> filters = new LinkedHashMap<>(); //key - filter name, value - filter type
    Map<String, FilterRegistration> filterRegistrations; // key - filter name, value - filter mapping
    Map<String, Class<?>> servlets = new LinkedHashMap<>(); //key - servlet name, value - servlet type
    Map<String, String> servletMappings = new LinkedHashMap<>(); //key = servlet name, value - url-pattern

    static {
        ORDERED_VALID_TAGS = new LinkedHashSet<>();
        ORDERED_VALID_TAGS.addAll(VALID_TAGS);

    }
    public ServletContext(String contextPath, Map<String, Class<?>> servlets, Map<String, String> servletMappings) {
        this.contextPath = contextPath;
        this.servlets = servlets;
        this.servletMappings = servletMappings;
        //parseWebXML(webXmlPath);
    }

    private void parseWebXML(String webXmlPath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document document = docBuilder.parse(webXmlPath);
        invalidateWebXML(document);
        invalidateNodes(document);
        parseFilters(document);
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

    private NodeList parseNodes(Document document, String tagName) throws IOException {
        NodeList nodeList = document.getElementsByTagName(tagName);
        int length = nodeList.getLength();
        if (length % 2 != 0) {
            throw new IOException("There is unclosed tag");
        }

        return nodeList;
    }

    private void parseFilters(Document document) throws IOException {
        NodeList filterNodes = parseNodes(document, "filter");
        Set<String> filterNames = new HashSet<>();

    }

    public String getContextPath() {
        return contextPath;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
       // Configuration configuration = new Configuration("/blogApp", "src/webapps/blogApp/src/main/webapp/WEB-INF/web.xml");
    }

    public Map<String, String> getServletMappings() {
        return servletMappings;
    }

    public Map<String, Class<?>> getServlets() {
        return servlets;
    }

    public Map<String, Class<?>> getFilters() {
        return filters;
    }

    public Map<String, FilterRegistration> getFilterRegistrations() {
        return filterRegistrations;
    }
}
