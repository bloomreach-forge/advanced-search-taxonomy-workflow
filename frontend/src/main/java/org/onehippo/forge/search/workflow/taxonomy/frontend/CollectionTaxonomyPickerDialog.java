package org.onehippo.forge.search.workflow.taxonomy.frontend;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.taxonomy.plugin.TaxonomyPickerDialog;
import org.onehippo.taxonomy.plugin.model.Classification;

/**
 * Created by charliechen on 11/24/15.
 */
public class CollectionTaxonomyPickerDialog extends TaxonomyPickerDialog {


    public CollectionTaxonomyPickerDialog(IPluginContext context, IPluginConfig config, IModel<Classification> model, String preferredLocale) {
        super(context, config, model, preferredLocale);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new PackageResourceReference(CollectionTaxonomyPickerDialog.class, CollectionTaxonomyPickerDialog.class.getSimpleName() + ".css")));
    }
}
