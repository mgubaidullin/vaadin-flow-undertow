package one.entropy.vaadinflowundertow;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.startup.RouteRegistryInitializer;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

public class MRouteRegistryInitializer extends RouteRegistryInitializer implements ServletContainerInitializer {

    private static final String PACKAGE_NAME = "one.entropy.vaadinflowundertow";

    @Override
    public void onStartup(Set<Class<?>> classSet, ServletContext servletContext) throws ServletException {
        try {
            File file = new File(MRouteRegistryInitializer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (file.isDirectory()){
                getClasses(PACKAGE_NAME, servletContext).stream()
                        .filter(clazz -> clazz.getAnnotation(Route.class) != null || clazz.getAnnotation(RouteAlias.class) != null)
                        .forEach(clazz -> classSet.add(clazz));
            } else {
                JarFile jarFile = new JarFile(file);
                Collections.list(jarFile.entries()).stream()
                        .filter(e -> e.getName().startsWith(PACKAGE_NAME.replace('.', '/')) && e.getName().endsWith(".class"))
                        .forEach(e -> {
                            try {
                                Class clazz = Class.forName(e.getName().replace("/", ".").replace(".class", ""));
                                if (clazz.getAnnotation(Route.class) != null || clazz.getAnnotation(RouteAlias.class) != null) {
                                    classSet.add(clazz);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onStartup(classSet, servletContext);
    }

    private List<Class<?>> getClasses(String packageName , ServletContext servletContext)  {
        try {
            ClassLoader classLoader = servletContext.getClassLoader();
            assert classLoader != null;
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            ArrayList<Class<?>> classes = new ArrayList();
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, packageName));
            }
            return classes;
        } catch (Exception ex){
            return new ArrayList(0);
        }
    }

    private  List findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
