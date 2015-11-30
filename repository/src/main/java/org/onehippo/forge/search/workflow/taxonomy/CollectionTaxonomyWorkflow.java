package org.onehippo.forge.search.workflow.taxonomy;

import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.onehippo.taxonomy.plugin.model.Classification;

import javax.jcr.RepositoryException;
import java.rmi.RemoteException;

/**
 * Created by charliechen on 11/12/15.
 */
public interface CollectionTaxonomyWorkflow extends Workflow {
    void setTaxonomy(Classification classification) throws WorkflowException, RepositoryException, RemoteException;
}
