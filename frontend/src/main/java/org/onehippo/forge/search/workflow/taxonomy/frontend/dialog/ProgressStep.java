/*
 * Copyright 2013-2017 Hippo B.V. (http://www.onehippo.com)
 */
package org.onehippo.forge.search.workflow.taxonomy.frontend.dialog;

import com.onehippo.cms7.search.frontend.ICollectionManager;
import com.onehippo.cms7.search.frontend.progressbar.ProgressBar;
import com.onehippo.cms7.search.frontend.progressbar.Progression;
import com.onehippo.cms7.search.frontend.progressbar.ProgressionModel;
import com.onehippo.cms7.search.frontend.workflow.ButtonBarStep;
import com.onehippo.cms7.search.state.Report;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;

public class ProgressStep extends ButtonBarStep {

    private final ProgressBar workflowProgressBar;

    public ProgressStep(final ICollectionManager collectionManager) {
        // add (de)publish progress bar
        ProgressionModel workflowProgressModel = new ProgressionModel() {
            @Override
            protected Progression getProgression() {
                int progression = 0;
                if (collectionManager.isExecuting()) {
                    Report report = collectionManager.getReport();
                    if (report.getTotalNumberOfDocuments() > 0) {
                        progression = (100 * report.getNumberOfProcessedDocuments()) / report.getTotalNumberOfDocuments();
                    }
                } else if (collectionManager.isReady()) {
                    progression = 100;
                }
                return new Progression(progression);
            }
        };

        workflowProgressBar = new ProgressBar("workflowProgress", workflowProgressModel) {
            @Override
            protected void onFinished(final AjaxRequestTarget target) {
                setComplete(true);
                getWizardModel().next();
            }
        };
        workflowProgressBar.setWidth(481);
        add(workflowProgressBar);

    }

    @Override
    public Component getButtonBar(final String id) {
        return new Fragment(id, "buttons", this) {
            {
                add(new AjaxButton("background") {
                    @Override
                    protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                        workflowProgressBar.stop();
                        getWizardModel().finish();
                    }
                });
            }
        };

    }

}
