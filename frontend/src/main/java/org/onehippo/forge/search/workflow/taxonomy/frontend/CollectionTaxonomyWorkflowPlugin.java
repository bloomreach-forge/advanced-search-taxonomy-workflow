package org.onehippo.forge.search.workflow.taxonomy.frontend;

import com.onehippo.cms7.search.frontend.ICollectionManager;
import com.onehippo.cms7.search.frontend.ISearchContext;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.*;
import org.hippoecm.addon.workflow.MenuDescription;
import org.hippoecm.addon.workflow.StdWorkflow;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JavaPluginConfig;
import org.hippoecm.frontend.plugins.standards.ClassResourceModel;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClassAppender;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.onehippo.forge.search.workflow.taxonomy.CollectionTaxonomyWorkflow;
import org.onehippo.forge.search.workflow.taxonomy.frontend.dialog.BulkWorkflowWizard;
import org.onehippo.taxonomy.api.Taxonomy;
import org.onehippo.taxonomy.plugin.ITaxonomyService;
import org.onehippo.taxonomy.plugin.model.Classification;
import org.onehippo.taxonomy.plugin.model.ClassificationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charliechen on 11/10/15.
 *
 * Last edited on July 6, 2018.
 */
public class CollectionTaxonomyWorkflowPlugin extends RenderPlugin {
    private static Logger log = LoggerFactory.getLogger(CollectionTaxonomyWorkflowPlugin.class);

    public CollectionTaxonomyWorkflowPlugin(final IPluginContext context,
                                            final IPluginConfig config) {
        super(context, config);
        WorkflowDescriptorModel model = (WorkflowDescriptorModel) getModel();

        final MarkupContainer content = new ActionsPanel("content");
        final TaxonomyWorkflow workflow = new TaxonomyWorkflow(context, config, model);
        workflow.setEnabled(getTaxonomy() != null);
        content.add(workflow);
        add(content);

        final TaxonomyMenuDescription menuDescription = new TaxonomyMenuDescription(content, Model.of(new ResourceModel("menu").wrapOnAssignment(this).getObject()));
        add(menuDescription);
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

        private List<String> taxonomyKeys = new ArrayList<>();

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
        }
    }

    private class TaxonomyWorkflow extends StdWorkflow<CollectionTaxonomyWorkflow> {
        private final IPluginContext context;
        private final IPluginConfig config;
        private final IDialogService dialogService;
        private final ISearchContext searcher;
        private final ICollectionManager collectionManager;
        private final IModel<WorkflowDescriptor> model;
        private final IBrowseService browseService;
        private final ClassificationDao dao;

        private BulkWorkflowWizard wizard;


        public TaxonomyWorkflow(IPluginContext context, IPluginConfig config, WorkflowDescriptorModel model) {
            super("setTaxonomy",
                    new ClassResourceModel("set-taxonomy", CollectionTaxonomyWorkflowPlugin.class, null),
                    (WorkflowDescriptorModel) new RenderPlugin(context, new JavaPluginConfig()).getModel());
            this.context = context;
            this.config = config;
            this.searcher = getSearcher(context);
            this.dialogService = getDialogService(context);
            this.collectionManager = getCollectionManager(context);
            this.browseService = getBrowseService(context);
            this.model = model;
            this.dao = getClassificationDao(context, config);
            if (dao == null) {
                log.warn("Service {} not found for id {}", ClassificationDao.class.getName(), "service.taxonomy.dao");
            }
        }

        public void setModel(IModel<WorkflowDescriptor> model) {
            setDefaultModel(model);
        }

        @Override
        protected IModel initModel() {
            return model;
        }

        @Override
        protected void execute() throws Exception {
            execute((WorkflowDescriptorModel) getDefaultModel());
        }

        @Override
        protected void invoke() {
            IDialogService.Dialog dialog = createRequestDialog();
            if (dialog != null) {
                dialogService.show(dialog);
            } else {
                try {
                    execute();
                } catch (Exception ex) {
                    log.info("Workflow call failed", ex);
                    dialogService.show(createResponseDialog(ex));
                }
            }
        }

        @Override
        protected IDialogService.Dialog createRequestDialog() {
            wizard = new BulkWorkflowWizard(context, config, new TaxonomyModel(), getPreferredLocale(), this, collectionManager, browseService);
            return wizard;
        }

        @Override
        protected String execute(CollectionTaxonomyWorkflow workflow) throws Exception {

            if (searcher != null) {
                searcher.saveCollection();
            }

            Classification classification = (Classification) wizard.getDefaultModelObject();
            if (classification != null) {
                try {
                    workflow.setTaxonomy(classification.getKeys(), dao);
                } catch (RepositoryException re) {
                    log.error("Could not set taxonomy on documents", re);
                }
            } else {
                log.warn("No taxonomy category selected");
            }
            return null;
        }

        private ISearchContext getSearcher(final IPluginContext context) {
            return context.getService(ISearchContext.class.getName(), ISearchContext.class);
        }

        private IBrowseService getBrowseService(final IPluginContext context) {
            return context.getService("service.browse", IBrowseService.class);
        }

        private IDialogService getDialogService(final IPluginContext context) {
            return context.getService("service.search.dialog", IDialogService.class);
        }

        private ICollectionManager getCollectionManager(IPluginContext context) {
            return context.getService(ICollectionManager.class.getName(), ICollectionManager.class);
        }

        private ClassificationDao getClassificationDao(final IPluginContext context, final IPluginConfig config) {
            return context.getService(
                    config.getString(ClassificationDao.SERVICE_ID, "service.taxonomy.dao"),
                    ClassificationDao.class);
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
