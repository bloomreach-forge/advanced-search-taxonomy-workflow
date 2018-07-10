/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 */
package org.onehippo.forge.search.workflow.taxonomy.frontend.dialog;

import com.onehippo.cms7.search.frontend.ICollectionManager;
import com.onehippo.cms7.search.frontend.report.ReportModel;
import com.onehippo.cms7.search.frontend.workflow.ButtonBarStep;

import javax.jcr.RepositoryException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.hippoecm.frontend.service.IBrowseService;
import org.onehippo.forge.search.workflow.taxonomy.frontend.dialog.report.ReportView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by charliechen on July 6, 2018.
 */
public class ReportStep extends ButtonBarStep {

    static final Logger log = LoggerFactory.getLogger(ReportStep.class);

    private final ICollectionManager collectionManager;

    public ReportStep(final ICollectionManager collectionManager, final IBrowseService browseService) {
        this.collectionManager = collectionManager;
        add(new ReportView("view", new ReportModel(collectionManager), browseService));
    }

    @Override
    public void applyState() {
        try {
            collectionManager.getWorkflow().cleanupReport();
            setComplete(true);
        } catch (RepositoryException e) {
            error("Unable to cleanup report");
            log.warn("Unable to cleanup report");
        }
    }

    @Override
    public Component getButtonBar(final String id) {
        return new Fragment(id, "buttons", this) {
            {
                add(new AjaxButton("close") {
                    @Override
                    protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                        applyState();

                        // if the step completed after applying the state, move the model onward
                        if (isComplete()) {
                            getWizardModel().finish();
                        }
                    }
                });
            }
        };

    }

}
