package one.entropy.vaadinflowundertow;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.startup.RouteRegistryInitializer;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import javax.servlet.ServletException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.undertow.servlet.Servlets.defaultContainer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;


/**
 * @author Marat Gubaidullin
 */
public class VaadinFlowUndertowServer {

    private static final Logger LOGGER = Logger.getLogger(VaadinFlowUndertowServer.class.getSimpleName());
    private static Undertow undertow;
    private static final String PATH = "/";
    private static final String WEB_FRAGMENT_XML = "/META-INF/web-fragment.xml";

    public static void main(String[] args) {
        LOGGER.info("Vaadin Flow Undertow starting");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Vaadin Flow Undertow stopping");
                undertow.stop();
                LOGGER.info("Vaadin Flow Undertow stopped");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }));
        try {
            startUndertow();
            LOGGER.info("Vaadin Flow Undertow started");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static void startUndertow() throws ServletException {
        ServletInfo servletInfo = new ServletInfo("VaadinServlet", VaadinServlet.class)
                .setAsyncSupported(true)
                .setLoadOnStartup(1)
                .addInitParam("productionMode", getProductionMode())
                .addMapping("/*");

        MRouteRegistryInitializer initializer = new MRouteRegistryInitializer();

        InstanceFactory<RouteRegistryInitializer> instanceFactory = new ImmediateInstanceFactory<>(initializer);

        ServletContainerInitializerInfo sciInfo = new ServletContainerInitializerInfo(RouteRegistryInitializer.class, instanceFactory, new HashSet<>());

        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(initializer.getClass().getClassLoader())
                .setContextPath(PATH)
                .setDeploymentName("vaadinflowundertow")
                .setDisplayName("Vaadin Flow Undertow")
                .setResourceManager(new ClassPathResourceManager(VaadinServlet.class.getClassLoader()))
                .addServlets(servletInfo)
                .addServletContainerInitializers(new ServletContainerInitializerInfo[]{sciInfo})
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, new WebSocketDeploymentInfo());

        DeploymentManager manager = defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();

        PathHandler path = Handlers.path(Handlers.redirect(PATH)).addPrefixPath(PATH, manager.start());

        Undertow.Builder builder = Undertow.builder().addHttpListener(8080, "0.0.0.0").setHandler(path);

        undertow = builder.build();
        undertow.start();
    }

    private static String getProductionMode() {
        try {
            InputStream streamSource = VaadinFlowUndertowServer.class.getResourceAsStream(WEB_FRAGMENT_XML);
            if (streamSource != null) {
                final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                XMLStreamReader streamReader = inputFactory.createXMLStreamReader(streamSource);
                while (streamReader.hasNext()) {
                    streamReader.next();
                    if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT
                            && streamReader.getLocalName().equals("param-value")
                            && streamReader.getElementText().equals("true")) {
                        return "true";
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.log(Level.SEVERE, "Can not read web-fragment.xml. Vaadin is running in DEBUG MODE.");
        }
        return "false";
    }
}
