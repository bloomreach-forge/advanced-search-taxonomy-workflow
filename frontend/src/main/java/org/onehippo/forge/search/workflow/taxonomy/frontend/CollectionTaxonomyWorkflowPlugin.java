package org.onehippo.forge.search.workflow.taxonomy.frontend;

import com.onehippo.cms7.search.frontend.ISearchContext;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.*;
import org.hippoecm.addon.workflow.IWorkflowInvoker;
import org.hippoecm.addon.workflow.MenuDescription;
import org.hippoecm.addon.workflow.StdWorkflow;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.ClassResourceModel;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClassAppender;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.onehippo.forge.search.workflow.taxonomy.CollectionTaxonomyWorkflow;
import org.onehippo.taxonomy.api.Taxonomy;
import org.onehippo.taxonomy.plugin.ITaxonomyService;
import org.onehippo.taxonomy.plugin.model.Classification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charliechen on 11/10/15.
 */
public class CollectionTaxonomyWorkflowPlugin extends RenderPlugin {
    private static Logger log = LoggerFactory.getLogger(CollectionTaxonomyWorkflowPlugin.class);

    public CollectionTaxonomyWorkflowPlugin(final IPluginContext context,
                                            final IPluginConfig config) {
        super(context, config);
        WorkflowDescriptorModel model = (WorkflowDescriptorModel) getModel();

        final MarkupContainer content = new ActionsPanel("content");
        final TaxonomyWorkflow workflow = new TaxonomyWorkflow(context, model);
        workflow.setEnabled(getTaxonomy() != null);
        content.add(workflow);
        add(content);

        final TaxonomyMenuDescription menuDescription = new TaxonomyMenuDescription(content, Model.of(new ResourceModel("menu").wrapOnAssignment(this).getObject()));
        add(menuDescription);
    }

    /**
     * Creates and returns taxonomy picker dialog instance. <p> If you want to provide a custom taxonomy picker plugin, you might want to override this method.
     * </p>
     */
    private AbstractDialog<Classification> createPickerDialog(Model<Classification> model, String preferredLocale) {
        return new CollectionTaxonomyPickerDialog(getPluginContext(), getPluginConfig(), model, preferredLocale);
    }

    private Taxonomy getTaxonomy() {
        IPluginConfig config = getPluginConfig();
        ITaxonomyService service = getPluginContext()
                .getService(config.getString(ITaxonomyService.SERVICE_ID, ITaxonomyService.DEFAULT_SERVICE_TAXONOMY_ID), ITaxonomyService.class);

        final String taxonomyName = config.getString(ITaxonomyService.TAXONOMY_NAME);

        if (StringUtils.isBlank(taxonomyName)) {
            log.info("No configured/chosen taxonomy name. Found '{}'", taxonomyName);
            return null;
        }

        return service.getTaxonomy(taxonomyName);
    }

    /**
     * Returns the translation locale of the document if exists. Otherwise, returns the user's UI locale as a fallback.
     */
    private String getPreferredLocale() {
        return getLocale().getLanguage();
    }

    private class TaxonomyModel extends Model<Classification> {

        private final IWorkflowInvoker invoker;
        private final ISearchContext searcher;
        private List<String> taxonomyKeys = new ArrayList<>();

        public TaxonomyModel(IWorkflowInvoker invoker, ISearchContext searcher) {
            this.invoker = invoker;
            this.searcher = searcher;
        }

        @Override
        public Classification getObject() {
            return new Classification(taxonomyKeys, new IDetachable() {
                @Override
                public void detach() {
                }
            });
        }

        @Override
        public void setObject(Classification object) {
            super.setObject(object);
            taxonomyKeys = object.getKeys();
            if (searcher != null) {
                searcher.saveCollection();
            }
            try {
                invoker.invokeWorkflow();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private class TaxonomyWorkflow extends StdWorkflow<CollectionTaxonomyWorkflow> {
        private final ISearchContext searcher;
        private final TaxonomyModel taxonomyModel;

        public TaxonomyWorkflow(IPluginContext context, WorkflowDescriptorModel model) {
            super("setTaxonomy", new ClassResourceModel("set-taxonomy", CollectionTaxonomyWorkflowPlugin.class, null), context, model);
            searcher = getSearcher(context);
            taxonomyModel = new TaxonomyModel(this, searcher);
        }

        @Override
        protected IDialogService.Dialog createRequestDialog() {
            return createPickerDialog(taxonomyModel, getPreferredLocale());
        }

        @Override
        protected String execute(CollectionTaxonomyWorkflow workflow)
                throws Exception {

            if (searcher != null) {
                searcher.saveCollection();
            }
            workflow.setTaxonomy(taxonomyModel.getObject());

            return null;
        }

        private ISearchContext getSearcher(final IPluginContext context) {
            return context.getService(ISearchContext.class.getName(), ISearchContext.class);
        }
    }

    private class TaxonomyMenuDescription extends MenuDescription {

        private final MarkupContainer content;
        private final IModel<String> menuLabelModel;

        TaxonomyMenuDescription(MarkupContainer content, IModel<String> menuLabelModel) {
            this.content = content;
            this.menuLabelModel = menuLabelModel;
        }

        @Override
        public Component getLabel() {
            return new Label("label", menuLabelModel).add(new CssClassAppender(new LoadableDetachableModel<String>() {

                @Override
                protected String load() {
                    if (getTaxonomy() == null) {
                        return "disabled";
                    }

                    return null;
                }
            }));
        }

        public MarkupContainer getContent() {
            return content;
        }
    }

}
