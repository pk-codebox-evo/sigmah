package org.activityinfo.client.common.nav;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import org.activityinfo.client.AppEvents;
import org.activityinfo.client.EventBus;
import org.activityinfo.client.Place;
import org.activityinfo.client.event.NavigationEvent;
import org.activityinfo.client.page.PageManager;

public class NavigationPanel extends ContentPanel {


    protected final EventBus eventBus;

    protected TreePanel<Link> tree;
    protected TreeLoader<Link> loader;
    protected TreeStore<Link> store;

    protected Listener<NavigationEvent> navListener;
    protected Listener<BaseEvent> changeListener;

    public NavigationPanel(final EventBus eventBus, final Navigator navigator) {

        this.eventBus = eventBus;

        this.setHeading(navigator.getHeading());
        this.setScrollMode(Scroll.AUTOY);
        this.setLayout(new FitLayout());
        

        loader = new BaseTreeLoader<Link>(navigator) {
            @Override
            public boolean hasChildren(Link parent) {
                return navigator.hasChildren(parent);
            }
        };

        store = new TreeStore<Link>(loader);
        store.setKeyProvider(new ModelKeyProvider<Link>() {
            @Override
            public String getKey(Link link) {
                if(link.getParent() == null) {
                    return link.getName();
                } else {
                    return ((Link)link.getParent()).getName() + "/" + link.getName();
                }
            }
        });
        tree = new TreePanel<Link>(store);
        tree.setStateful(true);
        tree.setStateId(navigator.getStateId());
        tree.setDisplayProperty("name");
        tree.setAutoLoad(true);
        tree.setIconProvider(new ModelIconProvider<Link>() {
            @Override
            public AbstractImagePrototype getIcon(Link model) {
                return model.getIcon();
            }
        });

        tree.addListener(Events.OnClick, new Listener<TreePanelEvent<Link>>() {
            @Override
            public void handleEvent(TreePanelEvent<Link> tpe) {

                if(tpe.getItem().getPlace() != null) {
                    eventBus.fireEvent(new NavigationEvent(PageManager.NavigationRequested, tpe.getItem().getPlace()));
                }
            }
        });

        navListener = new Listener<NavigationEvent>() {
            public void handleEvent(NavigationEvent be) {
                onNavigated(be.getPlace());
            }
        };
        eventBus.addListener(PageManager.NavigationAgreed, navListener);


        changeListener = new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                loader.load();
            }
        };
        eventBus.addListener(AppEvents.SchemaChanged, changeListener);

        this.add(tree);
    }

    
    public void shutdown() {
        eventBus.removeListener(PageManager.NavigationAgreed, navListener);
        eventBus.removeListener(AppEvents.SchemaChanged, changeListener);
    }

    private void onNavigated(Place place) {
        for(Link link : store.getAllItems()) {
            if(link.getPlace() != null && link.getPlace().equals(place)) {
                ensureVisible(link);
            }
        }
    }

    public void ensureVisible(final Link link) {
        if(tree.isRendered()) {
            doExpandParents(link);
        } else {
            tree.addListener(Events.Render, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    doExpandParents(link);
                    tree.removeListener(Events.Render, this);

                }
            });
        }
    }

    private void doExpandParents(Link link) {
        Link parent = store.getParent(link);

        tree.getSelectionModel().select(link, false);

        while(parent != null) {
            tree.setExpanded(parent, true);
            parent = store.getParent(parent);
        }
    }

}