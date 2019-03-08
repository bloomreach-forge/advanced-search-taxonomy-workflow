package org.onehippo.forge.search.workflow.taxonomy.frontend.dialog.report;

import com.onehippo.cms7.search.frontend.report.FailureModel;
import com.onehippo.cms7.search.state.Failure;
import com.onehippo.cms7.search.state.Report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.NodeNameModel;
import org.hippoecm.frontend.model.event.IObservable;
import org.hippoecm.frontend.plugins.standards.list.ListColumn;
import org.hippoecm.frontend.plugins.standards.list.TableDefinition;
import org.hippoecm.frontend.plugins.standards.list.datatable.ListDataTable;
import org.hippoecm.frontend.plugins.standards.list.datatable.ListPagingDefinition;
import org.hippoecm.frontend.plugins.standards.list.datatable.SortableDataProvider;
import org.hippoecm.frontend.plugins.standards.list.resolvers.AbstractListAttributeModifier;
import org.hippoecm.frontend.plugins.standards.list.resolvers.DocumentIconAndStateRenderer;
import org.hippoecm.frontend.plugins.standards.list.resolvers.IListCellRenderer;
import org.hippoecm.frontend.plugins.standards.list.resolvers.StateIconAttributeModifier;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.skin.DocumentListColumn;
import org.hippoecm.frontend.util.DocumentUtils;

/**
 * Created by charliechen on July 6, 2018.
 */
public class ReportView extends Panel {

    private static final String REPORT_CONTAINER_ID = "report";
    private static final AttributeModifier[] NO_ATTRIBUTE_MODIFIERS = new AttributeModifier[0];

    private TableDefinition<Failure> tableDefinition;
    private ListDataTable<Failure> dataTable;
    private ListPagingDefinition pagingDefinition;
    private final IBrowseService browseService;
    private final IModel<Report> reportModel;

    public ReportView(final String id, final IModel<Report> reportModel, final IBrowseService browseService) {
        super(id, reportModel);
        this.browseService = browseService;
        this.reportModel = reportModel;
        add(new EmptyPanel(REPORT_CONTAINER_ID));
        setOutputMarkupId(true);
    }

    @Override
    protected void onBeforeRender() {
        if (reportModel.getObject().hasFailures()) {
            replaceWithErrorFragment();
        } else {
            replaceWithSuccessFragment();
        }
        super.onBeforeRender();
    }

    private org.apache.wicket.markup.html.basic.Label createLabel(final String resourceKeySuffix) {
        return new org.apache.wicket.markup.html.basic.Label("message",
                new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        final Report report = reportModel.getObject();
                        final String resourceKey = report.getAction() + resourceKeySuffix;
                        return new StringResourceModel(resourceKey, ReportView.this)
                                .setParameters(report.getSucceeded())
                                .getString();
                    }
                });
    }

    private void replaceWithSuccessFragment() {
        replace(new Fragment(REPORT_CONTAINER_ID, "success", this) {
            {
                add(createLabel("-success"));
            }
        });
    }

    private void replaceWithErrorFragment() {
        replace(new Fragment(REPORT_CONTAINER_ID, "errors", this) {
            {
                add(createLabel("-error-message"));

                final ISortableDataProvider<Failure, String> dataProvider = new SortableDataProvider<Failure>() {

                    private java.util.List<Failure> failures;

                    private void load() {
                        if (failures == null) {
                            Report report = reportModel.getObject();
                            failures = report.getFailures();
                        }
                    }

                    @Override
                    public void detach() {
                        failures = null;
                    }

                    @Override
                    public Iterator<? extends Failure> iterator(final long first, final long count) {
                        load();
                        return failures.subList((int) first, (int) (first + count)).iterator();
                    }

                    @Override
                    public long size() {
                        load();
                        return failures.size();
                    }

                    @Override
                    public IModel<Failure> model(Failure failure) {
                        return new FailureModel(failure);
                    }

                };

                List<ListColumn<Failure>> columns = new ArrayList<>();

                ListColumn<Failure> column = new ListColumn<>(Model.of(""), "icon");
                final DocumentIconAndStateRenderer iconRenderer = new DocumentIconAndStateRenderer();
                column.setRenderer(new IListCellRenderer<Failure>() {
                    @Override
                    public Component getRenderer(final String id, final IModel<Failure> model) {
                        return iconRenderer.getRenderer(id, new JcrNodeModel(model.getObject().getNode()));
                    }

                    @Override
                    public IObservable getObservable(final IModel<Failure> model) {
                        return iconRenderer.getObservable(new JcrNodeModel(model.getObject().getNode()));
                    }
                });

                final StateIconAttributeModifier iconAttributeModifier = new StateIconAttributeModifier();
                column.setAttributeModifier(new AbstractListAttributeModifier<Failure>() {
                    @Override
                    public AttributeModifier[] getCellAttributeModifiers(final IModel<Failure> model) {
                        final JcrNodeModel nodeModel = new JcrNodeModel(model.getObject().getNode());
                        return iconAttributeModifier.getCellAttributeModifiers(nodeModel);
                    }
                });
                column.setCssClass(DocumentListColumn.ICON.getCssClass());

                columns.add(column);

                column = new ListColumn<>(new ResourceModel("document-name"), "name");
                column.setRenderer(new IListCellRenderer<Failure>() {

                    private IModel<String> getDisplayName(Failure failure) {
                        try {
                            return DocumentUtils.getDocumentNameModel(failure.getNode());
                        } catch (RepositoryException ignored) {
                        }
                        return new NodeNameModel(new JcrNodeModel(failure.getNode()));
                    }

                    public Component getRenderer(String id, IModel<Failure> model) {
                        Failure failure = model.getObject();
                        return new org.apache.wicket.markup.html.basic.Label(id, getDisplayName(failure));
                    }

                    public IObservable getObservable(IModel<Failure> model) {
                        Failure failure = model.getObject();
                        IModel<String> displayName = getDisplayName(failure);
                        if (displayName instanceof IObservable) {
                            return (IObservable) displayName;
                        }
                        return null;
                    }
                });
                column.setCssClass(DocumentListColumn.NAME.getCssClass());
                columns.add(column);

                column = new ListColumn<>(new ResourceModel("reason"), "reason");
                column.setRenderer(new IListCellRenderer<Failure>() {

                    public Component getRenderer(String id, IModel<Failure> model) {
                        Failure failure = model.getObject();
                        return new Label(id, new ResourceModel(failure.getReason()));
                    }

                    public IObservable getObservable(IModel<Failure> model) {
                        return null;
                    }
                });
                column.setCssClass("doclisting-reason");
                columns.add(column);

                tableDefinition = new TableDefinition<>(columns, true);
                pagingDefinition = new ListPagingDefinition();
                pagingDefinition.setPageSize(12);

                ListDataTable.TableSelectionListener<Failure> selectionListener = model -> {
                    final Node node = model.getObject().getNode();
                    browseService.browse(new JcrNodeModel(node));
                };
                dataTable = new ListDataTable<Failure>("failed", tableDefinition, dataProvider, selectionListener,
                        false, pagingDefinition) {
                    @Override
                    public boolean isVisible() {
                        return dataProvider.size() > 0;
                    }
                };

                add(dataTable);
            }
        });
    }
}
