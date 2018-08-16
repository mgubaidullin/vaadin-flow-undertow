package one.entropy.vaadinflowundertow;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@HtmlImport("frontend://styles/shared-styles.html")
@Route("")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class MainLayout extends VerticalLayout {
    Label label = new Label("Hello world");

    public MainLayout() {
        add(label);
        setClassName("main-layout");
        setSizeFull();
        add(new Button("demo", event -> {System.out.println("done");}));
    }
}
