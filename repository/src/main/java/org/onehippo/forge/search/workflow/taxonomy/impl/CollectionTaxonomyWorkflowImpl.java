package org.onehippo.forge.search.workflow.taxonomy.impl;

import com.onehippo.cms7.search.state.Collection;
import com.onehippo.cms7.search.state.CollectionState;
import com.onehippo.cms7.search.state.Failure;
import com.onehippo.cms7.search.state.Report;
import com.onehippo.cms7.search.workflow.CollectionWorkflowReasons;
import com.onehippo.cms7.search.workflow.ReportWorkflow;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowContext;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.ext.InternalWorkflow;
import org.hippoecm.repository.ext.WorkflowImpl;
import org.onehippo.forge.search.workflow.taxonomy.CollectionTaxonomyWorkflow;
import org.onehippo.repository.documentworkflow.DocumentWorkflow;
import org.onehippo.taxonomy.plugin.model.Classification;
import org.onehippo.taxonomy.plugin.model.ClassificationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charliechen on 11/12/15.
 *
 * Last edited July 5, 2018.
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
        this.subject = rootSession.getNodeByIdentifier(subject.getIdentifier());
        this.state = new CollectionState(this.subject);
    }


    @Override
    public void setTaxonomy(List<String> taxonomyKeys, ClassificationDao dao)
            throws WorkflowException, RepositoryException, RemoteException {
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
                        Failure failure = setTaxonomyToDocument(documentWorkflow, taxonomyKeys, handle, dao);
                        if (failure == null) {
                            succeeded++;
                        } else {
                            report.addFailure(failure);
                        }
                    } else {
                        report.addFailure(new Failure(handle, CollectionWorkflowReasons.INVALID_WORKFLOW));
                    }
                } catch (ItemNotFoundException e) {
                    report.addFailure(new Failure(null, CollectionWorkflowReasons.DOCUMENT_NOT_FOUND));
                } catch (RepositoryException e) {
                    report.addFailure(new Failure(null, CollectionWorkflowReasons.WORKFLOW_FAILED));
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

    private Failure setTaxonomyToDocument(DocumentWorkflow workflow, List<String> keys, Node handle, ClassificationDao dao)
            throws RepositoryException {
        try {
            final Document document = workflow.obtainEditableInstance();
            final Node editableNode = document.getNode(session);

            if (dao != null) {
                Classification classification = dao.getClassification(new JcrNodeModel(editableNode));
                classification.getKeys().clear();
                classification.getKeys().addAll(keys);
                dao.save(classification);
                session.save();
                workflow.commitEditableInstance();
                return null;
            }

            log.warn("Unable to set taxonomy: service class {} not found", ClassificationDao.class.getName());
            return new Failure(handle, CollectionWorkflowReasons.INVALID_STATE);
        } catch (IllegalStateException e) {
            log.warn("Unable to set taxonomy: Not a classifiable document at {}", handle.getPath());
            return new Failure(handle, CollectionWorkflowReasons.INVALID_WORKFLOW);
        } catch (Exception e) {
            log.error("Failed to execute workflow on document at {}", handle.getPath(), e);
            return new Failure(handle, CollectionWorkflowReasons.WORKFLOW_FAILED);
        }
    }

    @Override
    public Map<String, Serializable> hints() throws WorkflowException {
        Map<String, Serializable> hints = super.hints();
        hints.put("setTaxonomies", canStart());
        return hints;
    }

    private boolean canStart() {
        if (state.getWorkflowPhase() == CollectionState.Phase.INITIALIZED) {
            return true;
        }
        if (state.getWorkflowPhase() == CollectionState.Phase.COMPLETE) {
            Report report = state.getReport();
            return !report.hasFailures();
        }
        return false;
    }
}
