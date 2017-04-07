package org.onehippo.forge.search.workflow.taxonomy.demo.beans;

import java.util.Calendar;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

@HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:newsdocument")
@Node(jcrType = "taxonomybulkwfdemo:newsdocument")
public class NewsDocument extends HippoDocument {
    /** 
     * The document type of the news document.
     */
    public final static String DOCUMENT_TYPE = "taxonomybulkwfdemo:newsdocument";
    private final static String TITLE = "taxonomybulkwfdemo:title";
    private final static String DATE = "taxonomybulkwfdemo:date";
    private final static String INTRODUCTION = "taxonomybulkwfdemo:introduction";
    private final static String IMAGE = "taxonomybulkwfdemo:image";
    private final static String CONTENT = "taxonomybulkwfdemo:content";
    private final static String LOCATION = "taxonomybulkwfdemo:location";
    private final static String AUTHOR = "taxonomybulkwfdemo:author";
    private final static String SOURCE = "taxonomybulkwfdemo:source";

    /** 
     * Get the title of the document.
     * @return the title
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:title")
    public String getTitle() {
        return getProperty(TITLE);
    }

    /** 
     * Get the date of the document.
     * @return the date
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:date")
    public Calendar getDate() {
        return getProperty(DATE);
    }

    /** 
     * Get the introduction of the document.
     * @return the introduction
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:introduction")
    public String getIntroduction() {
        return getProperty(INTRODUCTION);
    }

    /** 
     * Get the image of the document.
     * @return the image
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:image")
    public HippoGalleryImageSet getImage() {
        return getLinkedBean(IMAGE, HippoGalleryImageSet.class);
    }

    /** 
     * Get the main content of the document.
     * @return the content
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:content")
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    /** 
     * Get the location of the document.
     * @return the location
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:location")
    public String getLocation() {
        return getProperty(LOCATION);
    }

    /** 
     * Get the author of the document.
     * @return the author
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:author")
    public String getAuthor() {
        return getProperty(AUTHOR);
    }

    /** 
     * Get the source of the document.
     * @return the source
     */
    @HippoEssentialsGenerated(internalName = "taxonomybulkwfdemo:source")
    public String getSource() {
        return getProperty(SOURCE);
    }

    @HippoEssentialsGenerated(internalName = "hippotaxonomy:keys")
    public String[] getKeys() {
        return getProperty("hippotaxonomy:keys");
    }
}
