package tomcat.servlet_context;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.util.*;

public class WebXmlParser {
    private static final String WEB_XML_URL = "WEB-INF/web.xml";
    private static final List<String> VALID_TAGS = List.of("filter", "filter-mapping", "servlet", "servlet-mapping");
    private static final Set<String> ORDERED_VALID_TAGS;

    static {
        ORDERED_VALID_TAGS = new LinkedHashSet<>();
        ORDERED_VALID_TAGS.addAll(VALID_TAGS);
    }

    private final ServletContext servletContext;
    private final String webRoot;

    public WebXmlParser(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webRoot = servletContext.webRoot;
    }

    public void parseWebXML() throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            String fullWebXmlPath = webRoot + "/" + WEB_XML_URL;
            Document document = docBuilder.parse(fullWebXmlPath);
            invalidateWebXML(document);
            invalidateNodes(document);
            createServlets(document);
            createServletMappings(document);
            createFilters(document);
            createFilterMappings(document);
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

    private void createServlets(Document document) throws IOException, ClassNotFoundException {
        Set<String> servletNames = new LinkedHashSet<>();
        Set<Class<?>> servletClasses = new LinkedHashSet<>();
        parseObjects(document, servletNames, servletClasses, "servlet", "servlet-name", "servlet-class");

        Iterator<String> namesIt = servletNames.iterator();
        Iterator<Class<?>> classesIt = servletClasses.iterator();
        while (namesIt.hasNext() && classesIt.hasNext()) {
            servletContext.addServlet(namesIt.next(), classesIt.next());
        }
    }

    private void createFilters(Document document) throws IOException, ClassNotFoundException {
        Set<String> filterNames = new LinkedHashSet<>();
        Set<Class<?>> filterClasses = new LinkedHashSet<>();
        parseObjects(document, filterNames, filterClasses, "filter", "filter-name", "filter-class");

        Iterator<String> namesIt = filterNames.iterator();
        Iterator<Class<?>> classesIt = filterClasses.iterator();
        while (namesIt.hasNext() && classesIt.hasNext()) {
            servletContext.addFilter(namesIt.next(), classesIt.next());
        }
    }

    private void parseObjects(Document document, Set<String> names, Set<Class<?>> classes, String tagName, String nameAttr, String classAttr) throws IOException, ClassNotFoundException {
        NodeList servletNodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < servletNodes.getLength(); i++) {
            Node servletNode = servletNodes.item(i);
            NodeList servletAttr = servletNode.getChildNodes();
            for (int j = 0; j < servletAttr.getLength(); j++) {
                Node attrNode = servletAttr.item(j);

                if (attrNode instanceof Text) {
                    continue;
                }

                String attrName = attrNode.getNodeName();
                if (attrName.equals(nameAttr)) {
                    addName(attrNode, names, tagName);
                    continue;
                }

                if (attrName.equals(classAttr)) {
                    addClass(attrNode, classes);
                    continue;
                }

                throw new IOException("Invalid node: " + attrName);
            }
        }
    }

    private void addName(Node attrNode, Set<String> names, String objectType) throws IOException {
        String name = attrNode.getTextContent();
        if (!names.add(name)) {
            throw new IOException(objectType + " with this name already exists: " + name);
        }
    }

    private void addClass(Node attrNode, Set<Class<?>> classes) throws IOException {
        String className = attrNode.getTextContent();
        try {
            Class<?> clazz = servletContext.classLoader.loadClass(className);
            if (!classes.add(clazz)) {
                throw new IOException("Class already exists: " + clazz);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found: " + className);
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

    private void createFilterMappings(Document document) throws IOException {
        List<Class<?>> filterClasses = new ArrayList<>();
        List<String> servletNames = new ArrayList<>();
        parseFilterMappings(document, filterClasses, servletNames);

        for (int i = 0; i < filterClasses.size(); i++) {
            Class<?> filterClass = filterClasses.get(i);
            String servletName = servletNames.get(i);
            servletContext.addFilterMapping(filterClass, servletName);
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
                    case "servlet-name" -> addServletNameInMapping(attrNode, servletNames, "Servlet");
                    case "url-pattern" -> addMapping(attrNode, urlPatterns);
                    default -> throw new IOException("Invalid node: " + attrName);
                }
            }
        }
    }

    private void parseFilterMappings(Document document, List<Class<?>> filterClasses, List<String> servletNames) throws IOException {
        NodeList filterNodes = document.getElementsByTagName("filter-mapping");
        for (int i = 0; i < filterNodes.getLength(); i++) {
            Node filterNode = filterNodes.item(i);
            NodeList filterAttr = filterNode.getChildNodes();
            for (int j = 0; j < filterAttr.getLength(); j++) {
                Node attrNode = filterAttr.item(j);

                if (attrNode instanceof Text) {
                    continue;
                }

                String attrName = attrNode.getNodeName();
                switch (attrName) {
                    case "filter-name" -> addFilterClassInMapping(attrNode, filterClasses, "Filter");
                    case "servlet-name" -> addMapping(attrNode, servletNames);
                    default -> throw new IOException("Invalid node: " + attrName);
                }
            }
        }
    }

    private void addMapping(Node attrNode, List<String> mappings) {
        String urlPattern = attrNode.getTextContent();
        mappings.add(urlPattern);
    }

    private void addServletNameInMapping(Node attrNode, List<String> names, String tagName) throws IOException {
        String servletName = attrNode.getTextContent();
        if (!servletContext.getServlets().containsKey(servletName)) {
            throw new IOException(tagName + " with this name doesn't exists: " + servletName);
        }
        names.add(servletName);
    }

    private void addFilterClassInMapping(Node attrNode, List<Class<?>> classes, String tagName) throws IOException {
        String filterName = attrNode.getTextContent();
        Class<?> filterClass = servletContext.getFilters().get(filterName);
        if (filterClass == null) {
            throw new IOException(tagName + " with this name doesn't exists: " + filterName);
        }
        classes.add(filterClass);
    }
}
