package org.onehippo.forge.search.workflow.taxonomy.frontend.dialog;

import com.onehippo.cms7.search.frontend.ICollectionManager;
import com.onehippo.cms7.search.frontend.workflow.ButtonBarContainer;
import com.onehippo.cms7.search.frontend.workflow.ButtonBarStep;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.wizard.IWizardStep;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.hippoecm.addon.workflow.IWorkflowInvoker;
import org.hippoecm.frontend.dialog.Wizard;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IBrowseService;
import org.onehippo.taxonomy.plugin.model.Classification;

/**
 * Created by charliechen on July 6, 2018.
 *
 * Last edited on July 9, 2018.
 */
public class BulkWorkflowWizard extends Wizard<Classification> {
    private MarkupContainer buttonBar;

    public BulkWorkflowWizard(IPluginContext context, IPluginConfig config, IModel<Classification> model, Locale preferredLocale,
            IWorkflowInvoker invoker, ICollectionManager collectionManager, final IBrowseService browseService) {
        super(model);

        final WizardModel wizardModel = new WizardModel() {

            @Override
            public boolean isNextAvailable() {
                return !isLastStep(getActiveStep());
            }

            @Override
            public boolean isPreviousAvailable() {
                return false;
            }

            @Override
            public boolean isCancelVisible() {
                return false;
            }
        };
        wizardModel.add(new PickerStep(context, config, model, preferredLocale, invoker));
        wizardModel.add(new ProgressStep(collectionManager));
        wizardModel.add(new ReportStep(collectionManager, browseService));
        init(wizardModel);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        final JavaScriptResourceReference modalJS = new JavaScriptResourceReference(ModalWindow.class, "res/modal.js");
        response.render(JavaScriptHeaderItem.forReference(modalJS));
        final CssResourceReference modalCSS = new CssResourceReference(BulkWorkflowWizard.class, "style.css");
        response.render(CssHeaderItem.forReference(modalCSS));
        super.renderHead(response);
    }

    @Override
    protected Component newButtonBar(final String id) {
        buttonBar = new ButtonBarContainer(id);
        return buttonBar;
    }

    @Override
    public void onActiveStepChanged(final IWizardStep newStep) {
        super.onActiveStepChanged(newStep);
        buttonBar.addOrReplace(((ButtonBarStep) newStep).getButtonBar("bar"));
    }

    @Override
    public IModel<String> getTitle() {
        return wrap(new ResourceModel("wizard.title"));
    }
}
