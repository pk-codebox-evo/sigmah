package org.activityinfo.clientjre.place.config.design;

import static org.easymock.EasyMock.*;
import org.activityinfo.client.page.config.design.Designer;
import org.activityinfo.client.common.dialog.FormDialogTether;
import org.activityinfo.client.common.dialog.FormDialogCallback;
import org.activityinfo.client.common.grid.ConfirmCallback;
import org.activityinfo.client.command.monitor.AsyncMonitor;
import org.activityinfo.shared.dto.UserDatabaseDTO;
import org.activityinfo.shared.dto.EntityDTO;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.data.ModelData;

import java.util.Map;
import java.util.HashMap;
/*
 * @author Alex Bertram
 */

public class MockDesignTree implements Designer.View {

    public ModelData selection = null;
    public Map<String, Object> newEntityProperties = new HashMap<String, Object>();

    public void init(Designer presenter, UserDatabaseDTO db, TreeStore store) {

    }

    public FormDialogTether showNewForm(EntityDTO entity, FormDialogCallback callback) {

        for(String property : newEntityProperties.keySet()) {
            ((ModelData)entity).set(property, newEntityProperties.get(property));
        }

        FormDialogTether tether = createNiceMock(FormDialogTether.class);
        replay(tether);

        callback.onValidated(tether);
        return tether;
    }

    protected void mockEditEntity(EntityDTO entity) {

    }

    public void setActionEnabled(String actionId, boolean enabled) {

    }

    public void confirmDeleteSelected(ConfirmCallback callback) {

    }

    public ModelData getSelection() {
        return selection;
    }

    public AsyncMonitor getDeletingMonitor() {
        return null;
    }

    public AsyncMonitor getSavingMonitor() {
        return null;
    }
}