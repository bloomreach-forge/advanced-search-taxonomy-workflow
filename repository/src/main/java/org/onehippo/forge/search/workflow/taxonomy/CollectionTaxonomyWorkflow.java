package org.onehippo.forge.search.workflow.taxonomy;

import java.rmi.RemoteException;
import java.util.List;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.onehippo.taxonomy.plugin.model.ClassificationDao;

/**
 * Created by charliechen on 11/12/15.
 *
 * Last edited July 5, 2018.
 */
public interface CollectionTaxonomyWorkflow extends Workflow {
    void setTaxonomy(List<String> taxonomyKeys, ClassificationDao dao)
            throws WorkflowException, RepositoryException, RemoteException;
}
