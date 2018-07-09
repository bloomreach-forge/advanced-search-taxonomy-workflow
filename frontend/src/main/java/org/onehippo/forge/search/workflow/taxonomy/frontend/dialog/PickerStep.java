package org.onehippo.forge.search.workflow.taxonomy.frontend.dialog;

import com.onehippo.cms7.search.frontend.workflow.ButtonBarStep;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.addon.workflow.IWorkflowInvoker;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.cms.browse.tree.FolderTreePlugin;
import org.hippoecm.frontend.plugins.standards.tree.icon.ITreeNodeIconProvider;
import org.onehippo.taxonomy.plugin.TaxonomyBrowser;
import org.onehippo.taxonomy.plugin.model.Classification;
import org.onehippo.taxonomy.plugin.model.TaxonomyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PickerStep extends ButtonBarStep {

    private static final Logger log = LoggerFactory.getLogger(PickerStep.class);

    private final IWorkflowInvoker invoker;

    /**
     * The panel where {@link TaxonomyBrowser} instance is located.
     */
    protected Panel browser;

    public PickerStep(IPluginContext context, IPluginConfig config, IModel<Classification> model,
            String preferredLocale, IWorkflowInvoker invoker) {
        ITreeNodeIconProvider iconProvider = FolderTreePlugin.newTreeNodeIconProvider(context, config);

        add(browser = new TaxonomyBrowser("content", new Model<>(model.getObject()),
                new TaxonomyModel(context, config), preferredLocale, false, iconProvider));

        this.invoker = invoker;
    }

    @Override
    public void applyState() {
        try {
            final BulkWorkflowWizard bulkWorkflowWizard = findParent(BulkWorkflowWizard.class);
            bulkWorkflowWizard.setDefaultModelObject(browser.getDefaultModelObject());
            invoker.invokeWorkflow();
            setComplete(true);
        } catch (Exception e) {
            error(e);
            setComplete(false);
        }
    }

    @Override
    public Component getButtonBar(String id) {
        return new Fragment(id, "buttons", this) {
            {
                add(new AjaxButton("ok") {
                    @Override
                    protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                        applyState();

                        // if the step completed after applying the state, move the model onward
                        if (isComplete()) {
                            getWizardModel().next();
                        }
                    }
                });
                add(new AjaxButton("cancel") {
                    @Override
                    protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                        getWizardModel().cancel();
                        setComplete(false);
                    }
                });
            }
        };
    }


}
