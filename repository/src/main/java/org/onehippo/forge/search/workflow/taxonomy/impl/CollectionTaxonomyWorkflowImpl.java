package org.onehippo.forge.search.workflow.taxonomy.impl;

import com.onehippo.cms7.search.state.Collection;
import com.onehippo.cms7.search.state.CollectionState;
import com.onehippo.cms7.search.state.Failure;
import com.onehippo.cms7.search.state.Report;
import com.onehippo.cms7.search.workflow.CollectionWorkflowReasons;
import com.onehippo.cms7.search.workflow.ReportWorkflow;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowContext;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.ext.InternalWorkflow;
import org.hippoecm.repository.ext.WorkflowImpl;
import org.onehippo.forge.search.workflow.taxonomy.CollectionTaxonomyWorkflow;
import org.onehippo.repository.documentworkflow.DocumentWorkflow;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.onehippo.taxonomy.plugin.model.Classification;
import org.onehippo.taxonomy.plugin.model.JcrHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by charliechen on 11/12/15.
 */
public class CollectionTaxonomyWorkflowImpl extends WorkflowImpl
        implements CollectionTaxonomyWorkflow, InternalWorkflow {

    static final Logger log = LoggerFactory.getLogger(CollectionTaxonomyWorkflowImpl.class);

    private final Session session;
    private final Node subject;
    private final CollectionState state;

    /**
     * All implementations of a work-flow must provide a single, no-argument constructor.
     *
     * @throws RemoteException mandatory exception that must be thrown by all Remote objects
     */
    public CollectionTaxonomyWorkflowImpl(Session userSession, Session rootSession, Node subject)
            throws RemoteException, RepositoryException {
        this.session = rootSession;
        this.subject =
                rootSession.getNodeByIdentifier(subject.getIdentifier());
        this.state = new CollectionState(this.subject);
    }


    @Override
    public void setTaxonomy(Classification classification) throws WorkflowException, RepositoryException, RemoteException {
        final WorkflowContext context = getWorkflowContext();

        ReportWorkflow reportWorkflow =
                (ReportWorkflow) context.getWorkflow("collection-reports");
        if (state.isReady()) {
            if (state.getReport().hasFailures()) {
                throw new WorkflowException(
                        "Cannot start workflow when existing failures have not been acknowledged");
            }
            reportWorkflow.cleanupReport();
        }

        reportWorkflow.createReport("setTaxonomy");
        Report report = state.getReport();
        Collection collection = state.getCollection();

        int succeeded = 0;
        int executed = 0;
        try {
            for (String handleId : collection.getDocumentIdentifiers()) {
                executed++;
                try {
                    Node handle = session.getNodeByIdentifier(handleId);
                    Workflow workflow =
                            context.getWorkflow("default", new Document(handle));
                    if (workflow instanceof DocumentWorkflow) {
                        DocumentWorkflow documentWorkflow = (DocumentWorkflow) workflow;
                        if (setTaxonomyToDocument(documentWorkflow, classification, handle.getPath())) {
                            succeeded++;
                        } else {
                            report.addFailure(new Failure(handle, CollectionWorkflowReasons.WORKFLOW_FAILED));
                        }
                    } else {
                        report.addFailure(new Failure(handle, CollectionWorkflowReasons.INVALID_WORKFLOW));
                    }
                } catch (ItemNotFoundException e) {
                    report.addFailure(new Failure(null, CollectionWorkflowReasons.DOCUMENT_NOT_FOUND));
                } finally {
                    report.setNumberOfProcessedDocuments(executed);
                    session.save();
                    session.refresh(false);
                }
            }
        } finally {
            reportWorkflow.finishReport(succeeded);
            session.save();
            session.refresh(false);
        }
    }

    private boolean setTaxonomyToDocument(DocumentWorkflow workflow, Classification classification, String handlePath) {
        try {
            final Document document = workflow.obtainEditableInstance();
            final Node editableNode = document.getNode(session);

            if (JcrHelper.isNodeType(editableNode, TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CLASSIFIABLE)) {
                List<String> keys = classification.getKeys();
                editableNode.setProperty("hippotaxonomy:keys", keys.toArray(new String[keys.size()]));
                if (JcrHelper.isNodeType(editableNode, TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CANONISED)) {
                    String canonKey = classification.getCanonical();
                    // when null, save empty string because it is mandatory at JCR level (see hippotaxonomy.cnd)
                    editableNode.setProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_CANONICALKEY, (canonKey == null) ? "" : canonKey);
                }
                session.save();
                workflow.commitEditableInstance();
                return true;
            }

            log.info("Unable to set taxonomy: Not a classifiable document at {}", handlePath);
            return false;
        } catch (Exception e) {
            log.error("Failed to execute workflow on document at {}",
                    handlePath, e);
            return false;
        }
    }
}
